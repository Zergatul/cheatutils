import * as FallbackLoader from '/fallback-loader.js'
import * as http from '/http.js'
import { withCss } from '/components/Loader.js'

const monaco = await FallbackLoader.monaco();
const languageId = 'cheatutils-scripting-language';
monaco.languages.register({ id: languageId });
monaco.languages.setLanguageConfiguration(languageId, {
    comments: { lineComment: '//', blockComment: ['/*', '*/'] },
    autoClosingPairs: [
        { open: '{', close: '}' }, { open: '[', close: ']' }, { open: '(', close: ')' },
        { open: '"', close: '"' }, { open: "'", close: "'" }
    ]
});
monaco.languages.setMonarchTokensProvider(languageId, {
    keywords: ['boolean', 'int', 'int64', 'float', 'string', 'char', 'void', 'if', 'else', 'for', 'foreach', 'while', 'return', 'break', 'continue', 'true', 'false', 'new', 'let', 'in'],
    tokenizer: {
        root: [
            [/\/\/.*$/, 'comment'], [/\/\*/, 'comment', '@comment'],
            [/"/, 'string', '@string'],
            [/[a-zA-Z_][\w]*/, { cases: { '@keywords': 'keyword', '@default': 'identifier' } }],
            [/\d+(\.\d+)?/, 'number']
        ],
        comment: [[/\*\//, 'comment', '@pop'], [/./, 'comment']],
        string: [[/[^\\"]+/, 'string'], [/\\./, 'string.escape'], [/"/, 'string', '@pop']]
    }
});

export function createComponent(template) {
    return withCss(import.meta.url, {
        template,
        props: { modelValue: { type: String, default: '' }, type: String, readOnly: Boolean },
        emits: ['update:modelValue'],
        data() { return { isFullscreen: false, error: '' }; },
        mounted() {
            // Keep Monaco objects outside Vue's reactive data.
            const model = monaco.editor.createModel(this.modelValue || '', languageId);
            const theme = window.matchMedia('(prefers-color-scheme: dark)');
            const editor = monaco.editor.create(this.$refs.editor, {
                model, readOnly: this.readOnly, automaticLayout: true, minimap: { enabled: false },
                theme: theme.matches ? 'vs-dark' : 'vs', fontSize: 14,
                scrollBeyondLastLine: false, tabSize: 4
            });
            let timer;
            let request = 0;
            let disposed = false;
            const diagnose = async () => {
                const sequence = ++request;
                const version = model.getVersionId();
                try {
                    const result = await http.post('/api/script-compile', { code: model.getValue() });
                    if (disposed || sequence !== request || version !== model.getVersionId()) return;
                    this.error = '';
                    monaco.editor.setModelMarkers(model, 'cheatutils', result.diagnostics.map(d => ({
                        startLineNumber: d.line, startColumn: d.column,
                        endLineNumber: d.endLine, endColumn: d.endColumn,
                        message: d.message, severity: monaco.MarkerSeverity.Error
                    })));
                } catch (error) {
                    if (!disposed && sequence === request) this.error = error.response || 'Cannot check script.';
                }
            };
            const listener = model.onDidChangeContent(() => {
                this.$emit('update:modelValue', model.getValue());
                clearTimeout(timer);
                timer = setTimeout(diagnose, 400);
            });
            const onTheme = () => monaco.editor.setTheme(theme.matches ? 'vs-dark' : 'vs');
            const onKey = event => {
                if (event.key === 'Escape' && this.isFullscreen) this.toggleFullscreen();
            };
            theme.addEventListener('change', onTheme);
            window.addEventListener('keydown', onKey);
            this._setCode = code => { if (code !== model.getValue()) model.setValue(code || ''); };
            this._setReadOnly = value => editor.updateOptions({ readOnly: value });
            this._dispose = () => {
                disposed = true;
                clearTimeout(timer);
                theme.removeEventListener('change', onTheme);
                window.removeEventListener('keydown', onKey);
                listener.dispose();
                editor.dispose();
                model.dispose();
            };
            diagnose();
        },
        beforeUnmount() {
            this._dispose?.();
            document.body.classList.remove('script-editor-fullscreen');
        },
        watch: {
            modelValue(value) { this._setCode?.(value); },
            readOnly(value) { this._setReadOnly?.(value); }
        },
        methods: {
            toggleFullscreen() {
                this.isFullscreen = !this.isFullscreen;
                document.body.classList.toggle('script-editor-fullscreen', this.isFullscreen);
            }
        }
    });
}
