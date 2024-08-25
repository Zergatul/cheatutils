import * as monaco from 'https://cdn.jsdelivr.net/npm/monaco-editor@0.48.0/+esm';
import * as http from '/http.js';

const languageId = 'cheatutils-scripting-language';
const map = [];

function getSettingsByModel(model) {
    return map.find(o => o.model == model);
}

let link = document.createElement('link');
link.href = 'https://cdn.jsdelivr.net/npm/vscode-codicons@0.0.17/dist/codicon.min.css';
link.rel = 'stylesheet';
document.head.appendChild(link);

let dark = null;

function isDark() {
    return dark && dark.matches;
}

function applyTheme() {
    if (isDark()) {
        monaco.editor.setTheme('cheatutils-scripting-language-dark');
    } else {
        monaco.editor.setTheme('cheatutils-scripting-language-light');
    }
}

if (window.matchMedia) {
    dark = window.matchMedia('(prefers-color-scheme: dark)');
    dark.addEventListener('change', () => {
        applyTheme();
    });
}

const languageSettingsContructor = (async () => {
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
        ]
    });

    const tokens = await http.get('/api/code/tokens');
    const nodes = await http.get('/api/code/nodes');

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

    monaco.languages.registerDocumentSemanticTokensProvider(languageId, {
        getLegend() {
            return {
                tokenTypes: tokens,
                tokenModifiers: [],
            };
        },
        async provideDocumentSemanticTokens(model, lastResultId, token) {
            let tokenize = http.post('/api/code/tokenize', model.getValue());
            setDiagnostics(model);
            let lexerOutput = await tokenize;
            let result = [];
            let prevToken = { range: { line1: 1, column1: 1, length: 0 } };
            for (let token of lexerOutput.tokens.list) {
                let type = tokens[token.type];
                if (type == 'WHITESPACE' || type == 'LINE_BREAK') {
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
        async provideDocumentColors(model, token) {
            let result = [];
            let lexerOutput = await http.post('/api/code/tokenize', model.getValue());
            for (let token of lexerOutput.tokens.list) {
                if (token.range.line1 == token.range.line2 && tokens[token.type] == 'STRING_LITERAL') {
                    let line = model.getLineContent(token.range.line1);
                    let value = line.substring(token.range.column1 - 1, token.range.column2 - 1);
                    if (value.match(/^"#[0-9a-fA-F]{6}"$/)) {
                        let r = parseInt(value.substring(2, 4), 16) / 255;
                        let g = parseInt(value.substring(4, 6), 16) / 255;
                        let b = parseInt(value.substring(6, 8), 16) / 255;
                        result.push({
                            color: { red: r, blue: b, green: g, alpha: 1 },
                            range: {
                                startLineNumber: token.range.line1,
                                startColumn: token.range.column1,
                                endLineNumber: token.range.line2,
                                endColumn: token.range.column2
                            }
                        });
                    }
                    if (value.match(/^"#[0-9a-fA-F]{8}"$/)) {
                        let r = parseInt(value.substring(2, 4), 16) / 255;
                        let g = parseInt(value.substring(4, 6), 16) / 255;
                        let b = parseInt(value.substring(6, 8), 16) / 255;
                        let a = parseInt(value.substring(8, 10), 16) / 255;
                        result.push({
                            color: { red: r, blue: b, green: g, alpha: a },
                            range: {
                                startLineNumber: token.range.line1,
                                startColumn: token.range.column1,
                                endLineNumber: token.range.line2,
                                endColumn: token.range.column2
                            }
                        });
                    }
                }
            }
            return result;
        }
    });

    monaco.editor.defineTheme('cheatutils-scripting-language-light', {
        base: 'vs',
        inherit: true,
        colors: {},
        rules: await http.get('/api/code/token-rules/light')
    });

    monaco.editor.defineTheme('cheatutils-scripting-language-dark', {
        base: 'vs-dark',
        inherit: true,
        colors: {},
        rules: await http.get('/api/code/token-rules/dark')
    });

    applyTheme();
})();

export function createComponent(template) {
    return {
        template: template,
        props: [
            'modelValue',
            'type'
        ],
        emits: ['update:modelValue'],
        async mounted() {
            await languageSettingsContructor;

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
}