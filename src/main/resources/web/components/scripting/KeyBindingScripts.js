import { getComponent, withCss } from '/components/Loader.js'
import * as http from '/http.js'

function diagnostics(response) {
    return (response.diagnostics || []).map(d => `Ln ${d.line}, Col ${d.column}: ${d.message}`).join('\n');
}

export function createComponent(template) {
    const args = {
        template,
        data() {
            return { mode: 'list', list: null, script: null, original: null, busy: false, error: '', message: '' };
        },
        created() { this.perform(() => this.refresh()); },
        methods: {
            async perform(action) {
                if (this.busy) return;
                this.busy = true;
                this.error = '';
                try { await action(); }
                catch (error) { this.error = error.response || error.message || String(error); }
                finally { this.busy = false; }
            },
            async refresh() {
                this.list = await http.get('/api/keybinding-scripts');
                this.mode = 'list';
            },
            add() {
                this.original = null;
                this.script = { name: '', code: 'ui.systemMessage("Hello");' };
                this.message = this.error = '';
                this.mode = 'add';
            },
            edit(script) {
                this.original = { ...script };
                this.script = { ...script };
                this.message = this.error = '';
                this.mode = 'edit';
            },
            back() {
                const changed = !this.original || this.original.name !== this.script.name || this.original.code !== this.script.code;
                if (!changed || confirm('Discard unsaved changes?')) this.mode = 'list';
            },
            assign(script, event) {
                const key = Number(event.target.value);
                event.target.value = script.key;
                this.perform(async () => {
                    await http.put(`/api/keybinding-scripts-assign/${encodeURIComponent(script.name)}`, key);
                    await this.refresh();
                });
            },
            remove(script) {
                if (!confirm(`Remove script "${script.name}"?`)) return;
                this.perform(async () => {
                    await http.delete(`/api/keybinding-scripts/${encodeURIComponent(script.name)}`);
                    await this.refresh();
                });
            },
            save() {
                this.perform(async () => {
                    const body = { name: this.script.name, code: this.script.code };
                    const response = this.mode === 'add'
                        ? await http.post('/api/keybinding-scripts', body)
                        : await http.put(`/api/keybinding-scripts/${encodeURIComponent(this.original.name)}`, body);
                    if (response.ok) await this.refresh();
                    else this.message = diagnostics(response);
                });
            },
            run() {
                this.perform(async () => {
                    const response = await http.post('/api/script-exec', { code: this.script.code });
                    this.message = response.success ? 'Executed successfully (not saved).' : response.error || diagnostics(response);
                });
            }
        }
    };
    args.components = { ScriptEditor: getComponent('common/ScriptEditor') };
    return withCss(import.meta.url, args);
}
