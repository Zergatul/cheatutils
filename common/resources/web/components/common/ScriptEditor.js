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
        wordPattern: /[A-Za-z0-9_#]+/
    });

    const tokenTypes = await http.get('/api/code/tokens');

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
                tokenModifiers: [],
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
                /*
                    Line number (0-indexed, and offset from the previous line)
                    Column position (0-indexed, and offset from the previous column, unless this is the beginning of a new line)
                    Token length
                    Token type index (0-indexed into the tokenTypes array defined in getLegend)
                    Modifier index (0-indexed into the tokenModifiers array defined in getLegend)
                */
                result.push(
                    token.range.line1 - prevToken.range.line1,
                    token.range.line1 == prevToken.range.line1 ? token.range.column1 - (prevToken.range.column1) : token.range.column1 - 1,
                    token.range.length,
                    token.type,
                    0);
    
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
            const theme = isDark() ? 'dark' : 'light';
            const hover = await http.post(`/api/code/hover/${theme}`, {
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
        triggerCharacters: ['.'],
        async provideCompletionItems(model, position, context, token) {
            const suggestions = await http.post('/api/code/completion', {
                code: model.getValue(),
                type: getSettingsByModel(model).type,
                line: position.lineNumber,
                column: position.column
            });
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
            type: 'keyword',
            foreground: '0000FF'
        }, {
            type: 'method',
            foreground: '795E26'
        }, {
            type: 'property',
            foreground: '001080'
        }, {
            type: 'variable',
            foreground: '001080'
        }, {
            type: 'type',
            foreground: '267F99'
        }, {
            type: 'delimiter',
            foreground: '000000'
        }, {
            type: 'operator',
            foreground: '000000'
        },{
            type: 'number',
            foreground: '098658'
        }, {
            type: 'string',
            foreground: 'A31515'
        }, {
            type: 'comment',
            foreground: '008000'
        }]
    });

    console.log(tokenTypes);

    monaco.editor.defineTheme('scripting-language-dark', {
        base: 'vs-dark',
        inherit: true,
        colors: {},
        rules: [{
            token: 'keyword',
            foreground: 'C586C0'
        }, {
            token: 'method',
            foreground: 'CBDBAB'
        }, {
            token: 'property',
            foreground: 'DBCBAB'
        }, {
            token: 'variable',
            foreground: 'DCDCAA'
        }, {
            token: 'type',
            foreground: '4EC9B0'
        }, {
            token: 'delimiter',
            foreground: 'D4D4D4'
        }, {
            token: 'operator',
            foreground: 'D4D4D4'
        },{
            token: 'number',
            foreground: 'B5CEA8'
        }, {
            token: 'string',
            foreground: 'CE9178'
        }, {
            token: 'comment',
            foreground: '6A9955'
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

            let element = this.$.subTree.el;
            this.editor = monaco.editor.create(element, {
                value: this.modelValue,
                language: languageId,
                'autoClosingBrackets': true,
                'semanticHighlighting.enabled': true,
                automaticLayout: true,       // automatically resize editor based on container size
                fixedOverflowWidgets: true,  // fix hover to be above parent->parent
                wordBasedSuggestions: 'off', // disable default all words from current document suggestions if API returns []
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
            this.editor.dispose();
            for (let i = 0; i < map.length; i++) {
                if (map[i].editor == this.editor) {
                    map.splice(i, 1);
                    i--;
                }
            }
        },
        methods: {},
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