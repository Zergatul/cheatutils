import * as FallbackLoader from '/fallback-loader.js'
import * as http from '/http.js'
import { withCss } from '/components/Loader.js'

const monaco = await FallbackLoader.monaco();

const languageId = 'cheatutils-scripting-language';
const modelSettings = [];

function getSettingsByModel(model) {
    return modelSettings.find(settings => settings.model == model);
}

function addStyleSheet(url) {
    if (document.querySelector(`link[href="${url}"]`)) {
        return;
    }
    const link = document.createElement('link');
    link.href = url;
    link.rel = 'stylesheet';
    document.head.appendChild(link);
}

addStyleSheet('/local/codicon.min.css');

let dark = null;

function isDark() {
    return dark && dark.matches;
}

function applyTheme() {
    monaco.editor.setTheme(isDark() ? 'scripting-language-dark' : 'scripting-language-light');
}

if (window.matchMedia) {
    dark = window.matchMedia('(prefers-color-scheme: dark)');
    dark.addEventListener('change', applyTheme);
}

function createCancellation(token) {
    const controller = new AbortController();
    const disposable = token?.onCancellationRequested(() => controller.abort());
    return {
        signal: controller.signal,
        dispose: () => disposable?.dispose()
    };
}

function isAborted(error) {
    return error instanceof DOMException && error.name == 'AbortError';
}

function createRequest(model, additional = {}) {
    const settings = getSettingsByModel(model);
    return {
        code: model.getValue(),
        type: settings.type,
        ...additional
    };
}

function scheduleDiagnostics(model) {
    const settings = getSettingsByModel(model);
    clearTimeout(settings.diagnosticsTimer);
    settings.diagnosticsController?.abort();
    settings.diagnosticsTimer = setTimeout(async () => {
        const controller = new AbortController();
        const version = model.getVersionId();
        const type = settings.type;
        settings.diagnosticsController = controller;
        try {
            const diagnostics = await http.post(
                '/api/code/diagnostics',
                createRequest(model),
                controller.signal);
            if (controller.signal.aborted || model.getVersionId() != version || settings.type != type) {
                return;
            }
            monaco.editor.setModelMarkers(model, 'cheatutils', diagnostics.map(diagnostic => ({
                startLineNumber: diagnostic.range.line1,
                startColumn: diagnostic.range.column1,
                endLineNumber: diagnostic.range.line2,
                endColumn: diagnostic.range.column2,
                message: diagnostic.message,
                severity: monaco.MarkerSeverity.Error
            })));
        } catch (error) {
            if (!isAborted(error)) {
                console.error('Cannot update script diagnostics.', error);
            }
        } finally {
            if (settings.diagnosticsController == controller) {
                settings.diagnosticsController = null;
            }
        }
    }, 150);
}

const languageSettingsConstructor = (async () => {
    monaco.languages.register({ id: languageId });

    monaco.languages.setLanguageConfiguration(languageId, {
        comments: {
            lineComment: '//',
            blockComment: ['/*', '*/']
        },
        autoClosingPairs: [
            { open: '{', close: '}' },
            { open: '[', close: ']' },
            { open: '(', close: ')' },
            { open: '"', close: '"' },
            { open: "'", close: "'" }
        ],
        colorizedBracketPairs: [
            ['{', '}'],
            ['[', ']'],
            ['(', ')']
        ],
        onEnterRules: [
            {
                beforeText: /^\s*.*{\s*$/,
                afterText: /^\s*}$/,
                action: {
                    indentAction: monaco.languages.IndentAction.IndentOutdent
                }
            },
            {
                beforeText: /^\s*(((else ?)?if|for|foreach|while)\s*\(.*\)\s*|else\s*)$/,
                action: {
                    indentAction: monaco.languages.IndentAction.Indent
                }
            }
        ]
    });

    const tokenTypes = await http.get('/api/code/token-types');
    const tokenModifiers = await http.get('/api/code/token-modifiers');

    monaco.languages.setMonarchTokensProvider(languageId, {
        tokenizer: {
            root: [
                [/(\/\/.*$)/, 'comment'],
                [/\/\*/, { token: 'comment', next: '@comment' }],
                [/"([^"\\]|\\.)*$/, 'string.invalid'],
                [/"/, { token: 'string.quote', bracket: '@open', next: '@string' }]
            ],
            comment: [
                [/\*\//, { token: 'comment', next: '@pop' }],
                [/./, 'comment']
            ],
            string: [
                [/[^\\"]+/, 'string'],
                [/\\./, 'string.escape'],
                [/"/, { token: 'string.quote', bracket: '@close', next: '@pop' }]
            ]
        }
    });

    monaco.languages.registerDocumentSemanticTokensProvider(languageId, {
        getLegend() {
            return {
                tokenTypes,
                tokenModifiers
            };
        },
        async provideDocumentSemanticTokens(model, lastResultId, token) {
            scheduleDiagnostics(model);
            const version = model.getVersionId();
            const type = getSettingsByModel(model).type;
            const cancellation = createCancellation(token);
            try {
                const tokens = await http.post('/api/code/tokenize', createRequest(model), cancellation.signal);
                if (cancellation.signal.aborted || model.getVersionId() != version || getSettingsByModel(model).type != type) {
                    return { data: [] };
                }
                const result = [];
                let previous = { range: { line1: 1, column1: 1 } };
                for (const current of tokens) {
                    if (current.range.length == 0) {
                        continue;
                    }
                    result.push(
                        current.range.line1 - previous.range.line1,
                        current.range.line1 == previous.range.line1
                            ? current.range.column1 - previous.range.column1
                            : current.range.column1 - 1,
                        current.range.length,
                        current.type,
                        current.modifiers);
                    previous = current;
                }
                return { data: result };
            } catch (error) {
                if (isAborted(error)) {
                    return { data: [] };
                }
                throw error;
            } finally {
                cancellation.dispose();
            }
        },
        releaseDocumentSemanticTokens() {}
    });

    monaco.languages.registerHoverProvider(languageId, {
        async provideHover(model, position, token) {
            const cancellation = createCancellation(token);
            try {
                const hover = await http.post('/api/code/hover', createRequest(model, {
                    line: position.lineNumber,
                    column: position.column
                }), cancellation.signal);
                if (cancellation.signal.aborted || hover == null) {
                    return null;
                }
                return {
                    range: new monaco.Range(
                        hover.range.line1,
                        hover.range.column1,
                        hover.range.line2,
                        hover.range.column2),
                    contents: hover.content.map(value => ({
                        value,
                        isTrusted: true,
                        supportHtml: true
                    }))
                };
            } catch (error) {
                if (isAborted(error)) {
                    return null;
                }
                throw error;
            } finally {
                cancellation.dispose();
            }
        }
    });

    monaco.languages.registerDefinitionProvider(languageId, {
        async provideDefinition(model, position, token) {
            const cancellation = createCancellation(token);
            try {
                const range = await http.post('/api/code/definition', createRequest(model, {
                    line: position.lineNumber,
                    column: position.column
                }), cancellation.signal);
                if (cancellation.signal.aborted || range == null) {
                    return null;
                }
                return {
                    uri: model.uri,
                    range: new monaco.Range(
                        range.line1,
                        range.column1,
                        range.line2,
                        range.column2)
                };
            } catch (error) {
                if (isAborted(error)) {
                    return null;
                }
                throw error;
            } finally {
                cancellation.dispose();
            }
        }
    });

    monaco.languages.registerCompletionItemProvider(languageId, {
        triggerCharacters: ['.', '#'],
        async provideCompletionItems(model, position, context, token) {
            const cancellation = createCancellation(token);
            try {
                const suggestions = await http.post('/api/code/completion', createRequest(model, {
                    line: position.lineNumber,
                    column: position.column
                }), cancellation.signal);
                if (cancellation.signal.aborted) {
                    return { suggestions: [] };
                }

                const word = model.getWordUntilPosition(position);
                const line = model.getLineContent(position.lineNumber);
                const hasHash = word.startColumn > 1 && line[word.startColumn - 2] == '#';
                if (hasHash) {
                    for (const suggestion of suggestions) {
                        if (suggestion.insertText.startsWith('#')) {
                            suggestion.insertText = suggestion.insertText.substring(1);
                        }
                    }
                }

                return {
                    suggestions: suggestions.map(suggestion => ({
                        ...suggestion,
                        kind: monaco.languages.CompletionItemKind[suggestion.kind]
                    }))
                };
            } catch (error) {
                if (isAborted(error)) {
                    return { suggestions: [] };
                }
                throw error;
            } finally {
                cancellation.dispose();
            }
        }
    });

    const hex = value => value.toString(16).toUpperCase().padStart(2, '0');

    monaco.languages.registerColorProvider(languageId, {
        provideColorPresentations(model, colorInfo) {
            const color = colorInfo.color;
            const red = hex(Math.round(color.red * 255));
            const green = hex(Math.round(color.green * 255));
            const blue = hex(Math.round(color.blue * 255));
            if (color.alpha == 1) {
                return [{ label: `"#${red}${green}${blue}"` }];
            }
            return [{ label: `"#${red}${green}${blue}${hex(Math.round(color.alpha * 255))}"` }];
        },
        async provideDocumentColors(model, token) {
            const cancellation = createCancellation(token);
            try {
                const colors = await http.post('/api/code/color-strings', model.getValue(), cancellation.signal);
                return cancellation.signal.aborted ? [] : colors;
            } catch (error) {
                if (isAborted(error)) {
                    return [];
                }
                throw error;
            } finally {
                cancellation.dispose();
            }
        }
    });

    monaco.editor.defineTheme('scripting-language-light', {
        base: 'vs',
        inherit: true,
        colors: {},
        rules: [
            { token: 'KEYWORD', foreground: 'AF00DB' },
            { token: 'METHOD', foreground: '795E26' },
            { token: 'PROPERTY', foreground: '19007F' },
            { token: 'IDENTIFIER', foreground: '001080' },
            { token: 'TYPE', foreground: '267F99' },
            { token: 'BRACKET' },
            { token: 'SEPARATOR', foreground: '000000' },
            { token: 'OPERATOR', foreground: '000000' },
            { token: 'NUMBER', foreground: '098658' },
            { token: 'STRING', foreground: 'A31515' },
            { token: 'COMMENT', foreground: '008000' },
            { token: 'KEYWORD.PREDEFINED_TYPE', foreground: '0000FF' },
            { token: 'KEYWORD.ASYNC', foreground: '0000FF' },
            { token: 'KEYWORD.OPERATOR_LIKE', foreground: '0000FF' },
            { token: 'KEYWORD.VALUE', foreground: '0000FF' },
            { token: 'IDENTIFIER.EXTERNAL', fontStyle: 'underline' },
            { token: 'IDENTIFIER.STATIC', fontStyle: 'bold' },
            { token: 'IDENTIFIER.FUNCTION', fontStyle: 'italic' }
        ]
    });

    monaco.editor.defineTheme('scripting-language-dark', {
        base: 'vs-dark',
        inherit: true,
        colors: {},
        rules: [
            { token: 'KEYWORD', foreground: 'C586C0' },
            { token: 'METHOD', foreground: 'CBDBAB' },
            { token: 'PROPERTY', foreground: 'DBCBAB' },
            { token: 'IDENTIFIER', foreground: 'DCDCAA' },
            { token: 'TYPE', foreground: '4EC9B0' },
            { token: 'BRACKET' },
            { token: 'SEPARATOR', foreground: 'D4D4D4' },
            { token: 'OPERATOR', foreground: 'D4D4D4' },
            { token: 'NUMBER', foreground: 'B5CEA8' },
            { token: 'STRING', foreground: 'CE9178' },
            { token: 'COMMENT', foreground: '6A9955' },
            { token: 'KEYWORD.PREDEFINED_TYPE', foreground: '569CD6' },
            { token: 'KEYWORD.ASYNC', foreground: '569CD6' },
            { token: 'KEYWORD.OPERATOR_LIKE', foreground: '569CD6' },
            { token: 'KEYWORD.VALUE', foreground: '569CD6' },
            { token: 'IDENTIFIER.EXTERNAL', fontStyle: 'underline' },
            { token: 'IDENTIFIER.STATIC', fontStyle: 'bold' },
            { token: 'IDENTIFIER.FUNCTION', fontStyle: 'italic' }
        ]
    });

    applyTheme();
})();

export function createComponent(template) {
    const args = {
        template,
        props: {
            modelValue: {
                type: String,
                required: true
            },
            type: {
                type: String,
                required: true
            }
        },
        emits: ['update:modelValue'],
        data() {
            return {
                isFullscreen: false
            };
        },
        async mounted() {
            this.isDisposed = false;
            await languageSettingsConstructor;

            let settings = await http.get('/api/monaco-editor-settings');
            if (this.isDisposed) {
                return;
            }
            if (settings.json) {
                try {
                    settings = JSON.parse(settings.json);
                } catch (error) {
                    console.error('Cannot parse Monaco settings.', error);
                    settings = {};
                }
            } else {
                settings = {};
            }

            const model = monaco.editor.createModel(this.modelValue, languageId);
            const entry = {
                editor: null,
                model,
                type: this.type,
                diagnosticsTimer: null,
                diagnosticsController: null
            };
            modelSettings.push(entry);
            try {
                this.editor = monaco.editor.create(this.$refs.editor, {
                    model,
                    'autoClosingBrackets': true,
                    'semanticHighlighting.enabled': true,
                    automaticLayout: true,
                    wordBasedSuggestions: 'off',
                    suggest: {
                        showStatusBar: true
                    },
                    ...settings
                });
                entry.editor = this.editor;
                this.editor.onDidBlurEditorWidget(() => {
                    this.$emit('update:modelValue', model.getValue());
                });
            } catch (error) {
                modelSettings.splice(modelSettings.indexOf(entry), 1);
                model.dispose();
                throw error;
            }
        },
        unmounted() {
            this.isDisposed = true;
            document.body.classList.remove('script-editor-fullscreen');
            const entry = modelSettings.find(settings => settings.editor == this.editor);
            if (entry) {
                clearTimeout(entry.diagnosticsTimer);
                entry.diagnosticsController?.abort();
                modelSettings.splice(modelSettings.indexOf(entry), 1);
            }
            this.editor?.dispose();
            entry?.model.dispose();
        },
        methods: {
            toggleFullscreen() {
                this.isFullscreen = !this.isFullscreen;
                document.body.classList.toggle('script-editor-fullscreen', this.isFullscreen);
                this.$nextTick(() => this.editor.focus());
            }
        },
        watch: {
            modelValue(value) {
                const model = this.editor?.getModel();
                if (model && model.getValue() != value) {
                    model.setValue(value);
                }
            },
            type(value) {
                const model = this.editor?.getModel();
                const settings = model && getSettingsByModel(model);
                if (settings) {
                    settings.type = value;
                    scheduleDiagnostics(model);
                    monaco.editor.setModelLanguage(model, languageId);
                }
            }
        }
    };
    return withCss(import.meta.url, args);
}