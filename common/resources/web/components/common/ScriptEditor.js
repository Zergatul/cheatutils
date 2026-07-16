import * as FallbackLoader from '/fallback-loader.js'
import * as http from '/http.js'
import { withCss } from '/components/Loader.js'

const monaco = await FallbackLoader.monaco();

const languageId = 'cheatutils-scripting-language';
const map = [];

function getSettingsByModel(model) {
    return map.find(o => o.model == model);
}

function addStyleSheet(url, onSuccess, onError) {
    let link = document.createElement('link');
    link.href = url;
    link.rel = 'stylesheet';
    if (onSuccess) {
        link.addEventListener('load', onSuccess);
    }
    if (onError) {
        link.addEventListener('error', onError);
    }
    document.head.appendChild(link);
}

addStyleSheet('https://cdn.jsdelivr.net/npm/vscode-codicons@0.0.17/dist/codicon.min.css', () => {
    console.debug('Codicon has been loaded from CDN.');
}, () => {
    addStyleSheet('/local/codicon.min.css', () => {
        console.debug('Codicon has been loaded from local server.');
    }, () => {
        console.error('Failed to load Codicon from local server.', error);
    });
});

let dark = null;

function isDark() {
    return dark && dark.matches;
}

function applyTheme() {
    if (isDark()) {
        monaco.editor.setTheme('scripting-language-dark');
    } else {
        monaco.editor.setTheme('scripting-language-light');
    }
}

if (window.matchMedia) {
    dark = window.matchMedia('(prefers-color-scheme: dark)');
    dark.addEventListener('change', () => {
        applyTheme();
    });
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
                /* before:
                    ... {<Enter>}
                */
                /* after:
                    ... {
                        <cursor>
                    }
                */
                beforeText: /^\s*.*{\s*$/,
                afterText: /^\s*}$/,
                action: {
                    indentAction: monaco.languages.IndentAction.IndentOutdent
                }
            },
            {
                // after: if (...)
                // after: for (...)
                // after: else
                beforeText: /^\s*(((else ?)?if|for|foreach|while)\s*\(.*\)\s*|else\s*)$/,
                action: {
                    indentAction: monaco.languages.IndentAction.Indent
                }
            }
        ],
        //wordPattern: /[A-Za-z0-9_#]+/
    });

    const tokenTypes = await http.get('/api/code/token-types');
    const tokenModifiers = await http.get('/api/code/token-modifiers');

    const setDiagnostics = async (model) => {
        let diagnostics = await http.post('/api/code/diagnostics', {
            code: model.getValue(),
            type: getSettingsByModel(model).type
        });
        let tokens = [];
        for (let diagnostic of diagnostics) {
            tokens.push({
                startLineNumber: diagnostic.range.line1,
                startColumn: diagnostic.range.column1,
                endLineNumber: diagnostic.range.line2,
                endColumn: diagnostic.range.column2,
                message: diagnostic.message,
                severity: monaco.MarkerSeverity.Error
            });
        }
        monaco.editor.setModelMarkers(model, 'owner', tokens);
    };

    monaco.languages.setMonarchTokensProvider(languageId, {
        tokenizer: {
            root: [
                // Comments (Single line)
                [/(\/\/.*$)/, 'comment'],

                // Multi-line comments
                [/\/\*/, { token: 'comment', next: '@comment' }],

                // Strings
                [/"([^"\\]|\\.)*$/, 'string.invalid'],  // Unclosed string
                [/"/, { token: 'string.quote', bracket: '@open', next: '@string' }]
            ],
    
            // Multi-line comment tokenizer
            comment: [
                [/\*\//, { token: 'comment', next: '@pop' }],
                [/./, 'comment']
            ],
    
            // String tokenizer
            string: [
                [/[^\\"]+/, 'string'],
                [/\\./, 'string.escape'],
                [/"/, { token: 'string.quote', bracket: '@close', next: '@pop' }]
            ],
        },
    });

    monaco.languages.registerDocumentSemanticTokensProvider(languageId, {
        getLegend() {
            return {
                tokenTypes: tokenTypes,
                tokenModifiers: tokenModifiers,
            };
        },
        async provideDocumentSemanticTokens(model, lastResultId, token) {
            let tokenize = http.post('/api/code/tokenize', {
                code: model.getValue(),
                type: getSettingsByModel(model).type
            });
            setDiagnostics(model);

            let tokens = await tokenize;
            let result = [];
            let prevToken = { range: { line1: 1, column1: 1, length: 0 } };
            for (let token of tokens) {
                if (token.range.length == 0) {
                    continue;
                }

                result.push(
                    token.range.line1 - prevToken.range.line1,
                    token.range.line1 == prevToken.range.line1 ? token.range.column1 - (prevToken.range.column1) : token.range.column1 - 1,
                    token.range.length,
                    token.type,
                    token.modifiers);
    
                prevToken = token;
            }
            return {
                data: result
            };
        },
        releaseDocumentSemanticTokens(resultId) {
    
        }
    });

    monaco.languages.registerHoverProvider(languageId, {
        async provideHover(model, position) {
            const hover = await http.post('/api/code/hover', {
                code: model.getValue(),
                type: getSettingsByModel(model).type,
                line: position.lineNumber,
                column: position.column
            });
            if (hover == null) {
                return null;
            }
            return {
                range: new monaco.Range(
                    hover.range.line1,
                    hover.range.column1,
                    hover.range.line2,
                    hover.range.column2),
                contents: hover.content.map(s => {
                    return {
                        value: s,
                        isTrusted: true,
                        supportHtml: true
                    };
                })
            };
        }
    });

    monaco.languages.registerDefinitionProvider(languageId, {
        async provideDefinition(model, position, token) {
            const range = await http.post('/api/code/definition', {
                code: model.getValue(),
                type: getSettingsByModel(model).type,
                line: position.lineNumber,
                column: position.column
            });
            if (range == null) {
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
        }
    });

    monaco.languages.registerCompletionItemProvider(languageId, {
        triggerCharacters: ['.', '#'],
        async provideCompletionItems(model, position, context, token) {
            const suggestions = await http.post('/api/code/completion', {
                code: model.getValue(),
                type: getSettingsByModel(model).type,
                line: position.lineNumber,
                column: position.column
            });

            const word = model.getWordUntilPosition(position);
            const line = model.getLineContent(position.lineNumber);
            const hasHash = word.startColumn > 1 && line[word.startColumn - 2] == '#';

            if (hasHash) {
                // '#' character is not word-like character
                // so we have to truncate it to not create '##' for #typeof/#cast/etc items
                for (const suggestion of suggestions) {
                    if (suggestion.insertText.startsWith('#')) {
                        suggestion.insertText = suggestion.insertText.substr(1);
                    }
                }
            }

            return {
                suggestions: suggestions.map(s => {
                    return {
                        ...s,
                        kind: monaco.languages.CompletionItemKind[s.kind]
                    };
                })
            };
        }
    });

    const hex = value => {
        if (value < 16) {
            return '0' + value.toString(16).toUpperCase();
        } else {
            return value.toString(16).toUpperCase();
        }
    }

    monaco.languages.registerColorProvider(languageId, {
        provideColorPresentations(model, colorInfo) {
            let color = colorInfo.color;
            let r = Math.round(color.red * 255);
            let g = Math.round(color.green * 255);
            let b = Math.round(color.blue * 255);
            if (color.alpha == 1) {
                return [{
                    label: `"#${hex(r)}${hex(g)}${hex(b)}"`
                }]
            } else {
                let a = Math.round(color.alpha * 255);
                return [{
                    label: `"#${hex(r)}${hex(g)}${hex(b)}${hex(a)}"`
                }];
            }
        },
        async provideDocumentColors(model) {
            return await http.post('/api/code/color-strings', model.getValue());
        }
    });

    monaco.editor.defineTheme('scripting-language-light', {
        base: 'vs',
        inherit: true,
        colors: {},
        rules: [{
            token: 'KEYWORD',
            foreground: 'AF00DB'
        }, {
            token: 'METHOD',
            foreground: '795E26'
        }, {
            token: 'PROPERTY',
            foreground: '19007F'
        }, {
            token: 'IDENTIFIER',
            foreground: '001080'
        }, {
            token: 'TYPE',
            foreground: '267F99'
        }, {
            token: 'BRACKET'
        }, {
            token: 'SEPARATOR',
            foreground: '000000'
        }, {
            token: 'OPERATOR',
            foreground: '000000'
        },{
            token: 'NUMBER',
            foreground: '098658'
        }, {
            token: 'STRING',
            foreground: 'A31515'
        }, {
            token: 'COMMENT',
            foreground: '008000'
        }, {
            token: 'KEYWORD.PREDEFINED_TYPE',
            foreground: '0000FF'
        }, {
            token: 'KEYWORD.ASYNC',
            foreground: '0000FF'
        }, {
            token: 'KEYWORD.OPERATOR_LIKE',
            foreground: '0000FF'
        }, {
            token: 'KEYWORD.VALUE',
            foreground: '0000FF'
        }, {
            token: 'IDENTIFIER.EXTERNAL',
            fontStyle: 'underline'
        }, {
            token: 'IDENTIFIER.STATIC',
            fontStyle: 'bold'
        }, {
            token: 'IDENTIFIER.FUNCTION',
            fontStyle: 'italic'
        }]
    });

    monaco.editor.defineTheme('scripting-language-dark', {
        base: 'vs-dark',
        inherit: true,
        colors: {},
        rules: [{
            token: 'KEYWORD',
            foreground: 'C586C0'
        }, {
            token: 'METHOD',
            foreground: 'CBDBAB'
        }, {
            token: 'PROPERTY',
            foreground: 'DBCBAB'
        }, {
            token: 'IDENTIFIER',
            foreground: 'DCDCAA'
        }, {
            token: 'TYPE',
            foreground: '4EC9B0'
        }, {
            token: 'BRACKET'
        }, {
            token: 'SEPARATOR',
            foreground: 'D4D4D4'
        }, {
            token: 'OPERATOR',
            foreground: 'D4D4D4'
        },{
            token: 'NUMBER',
            foreground: 'B5CEA8'
        }, {
            token: 'STRING',
            foreground: 'CE9178'
        }, {
            token: 'COMMENT',
            foreground: '6A9955'
        }, {
            token: 'KEYWORD.PREDEFINED_TYPE',
            foreground: '569CD6'
        }, {
            token: 'KEYWORD.ASYNC',
            foreground: '569CD6'
        }, {
            token: 'KEYWORD.OPERATOR_LIKE',
            foreground: '569CD6'
        }, {
            token: 'KEYWORD.VALUE',
            foreground: '569CD6'
        }, {
            token: 'IDENTIFIER.EXTERNAL',
            fontStyle: 'underline'
        }, {
            token: 'IDENTIFIER.STATIC',
            fontStyle: 'bold'
        }, {
            token: 'IDENTIFIER.FUNCTION',
            fontStyle: 'italic'
        }],
    });

    applyTheme();

})();

export function createComponent(template) {
    const args = {
        template: template,
        props: [
            'modelValue',
            'type'
        ],
        emits: ['update:modelValue'],
        data() {
            return {
                isFullscreen: false
            };
        },
        async mounted() {
            await languageSettingsConstructor;

            let settings = await http.get('/api/monaco-editor-settings');
            if (settings.json) {
                try {
                    settings = JSON.parse(settings.json);
                } catch {
                    console.error('Cannot parse monaco settings');
                    settings = {};
                }
            } else {
                settings = {};
            }

            let element = this.$refs.editor;
            this.editor = monaco.editor.create(element, {
                value: this.modelValue,
                language: languageId,
                'autoClosingBrackets': true,
                'semanticHighlighting.enabled': true,
                automaticLayout: true,       // automatically resize editor based on container size
                wordBasedSuggestions: 'off', // disable default all words from current document suggestions if API returns []
                suggest: {
                    showStatusBar: true
                },
                ...settings
            });
            this.editor.onDidBlurEditorWidget(() => {
                this.$emit('update:modelValue', this.editor.getModel().getValue());
            });
            map.push({
                editor: this.editor,
                model: this.editor.getModel(),
                type: this.type
            });
        },
        unmounted() {
            document.body.classList.remove('script-editor-fullscreen');
            this.editor.dispose();
            for (let i = 0; i < map.length; i++) {
                if (map[i].editor == this.editor) {
                    map.splice(i, 1);
                    i--;
                }
            }
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
                if (this.editor == null) {
                    return;
                }
                if (this.editor.getModel().getValue() != value) {
                    this.editor.getModel().setValue(value);
                }
            }
        }
    };
    return withCss(import.meta.url, args);
}