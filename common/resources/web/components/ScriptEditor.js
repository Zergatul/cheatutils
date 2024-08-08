import * as monaco from 'https://cdn.jsdelivr.net/npm/monaco-editor@0.48.0/+esm';

const languageId = 'cheatutils-scripting-language';
const map = [];

function getSettingsByModel(model) {
    return map.find(o => o.model == model);
}

let link = document.createElement('link');
link.href = 'https://cdn.jsdelivr.net/npm/vscode-codicons@0.0.17/dist/codicon.min.css';
link.rel = 'stylesheet';
document.head.appendChild(link);

(async () => {
    monaco.languages.register({ id: languageId });

    monaco.languages.setLanguageConfiguration(languageId, {
        autoClosingPairs: [
            { open: '{', close: '}' },
            { open: '[', close: ']' },
            { open: '(', close: ')' },
            { open: '"', close: '"' },
            { open: "'", close: "'" }
        ],
        /*colorizedBracketPairs: [
            ['{', '}'],
            ['[', ']'],
            ['(', ')']
        ],*/
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

    const get = async url => {
        const response = await fetch(url);
        return await response.json();
    };
    const post = async (url, body) => {
        const response = await fetch(url, { method: 'POST', body: JSON.stringify(body) });
        return await response.json();
    };

    const tokens = await get('/api/code/tokens');
    const nodes = await get('/api/code/nodes');

    const setDiagnostics = async (model) => {
        let diagnostics = await post('/api/code/diagnostics', {
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
            let tokenize = post('/api/code/tokenize', model.getValue());
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
            const hover = await post('/api/code/hover', {
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
            const range = await post('/api/code/definition', {
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
            const suggestions = await post('/api/code/completion', {
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

    monaco.editor.defineTheme('cheatutils-scripting-language-dark', {
        base: 'vs-dark',
        inherit: true,
        colors: {},
        rules: await get('/api/code/token-rules')
    });

    monaco.editor.setTheme('cheatutils-scripting-language-dark');
})();

function createComponent(template) {
    return {
        template: template,
        props: [
            'modelValue',
            'type'
        ],
        emits: ['update:modelValue'],
        mounted() {
            let element = this.$.subTree.el;
            this.editor = monaco.editor.create(element, {
                value: this.modelValue,
                language: languageId,
                'autoClosingBrackets': true,
                'renderWhitespace': 'all',
                'semanticHighlighting.enabled': true
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
            //themeListeners = themeListeners.filter(l => l != eventHandler);
        },
        methods: {
            /*onThemeChanged() {
                const darkTheme = 'ace/theme/one_dark';
                const lightTheme = 'ace/theme/github';
                this.editor.setTheme(isDark() ? darkTheme : lightTheme);
            }*/
        },
        watch: {
            modelValue(value) {
                if (this.editor.getModel().getValue() != value) {
                    this.editor.getModel().setValue(value);
                }
            }
        }
    };
}

/* OLD */

/*let dark = null;
let themeListeners = [];

if (window.matchMedia) {
    dark = window.matchMedia('(prefers-color-scheme: dark)');
    dark.addEventListener('change', () => {
        themeListeners.forEach(callback => callback());
    });
}

function isDark() {
    return dark && dark.matches;
}

function createComponent(template) {
    let eventHandler = null;
    return {
        template: template,
        props: ['modelValue'],
        emits: ['update:modelValue'],
        mounted() {
            let self = this;
            let element = self.$.subTree.el;
            self.editor = ace.edit(element);
            self.editor.setSelectionStyle('text');
            self.editor.setFontSize('16px');
            self.editor.setShowPrintMargin(false);
            self.editor.session.setMode('ace/mode/java');
            if (self.modelValue) {
                self.editor.setValue(self.modelValue, -1);
            }
            self.editor.on('change', () => {
                self.$emit('update:modelValue', self.editor.getValue());
            });

            eventHandler = self.onThemeChanged;
            themeListeners.push(eventHandler);
            eventHandler();
        },
        unmounted() {
            themeListeners = themeListeners.filter(l => l != eventHandler);
        },
        methods: {
            onThemeChanged() {
                const darkTheme = 'ace/theme/one_dark';
                const lightTheme = 'ace/theme/github';
                this.editor.setTheme(isDark() ? darkTheme : lightTheme);
            }
        },
        watch: {
            modelValue(value) {
                if (this.editor.getValue() != value) {
                    this.editor.setValue(value, -1);
                }
            }
        }
    };
}*/

export { createComponent }