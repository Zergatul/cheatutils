import { components } from '../../components.js'
import { withCss } from '/components/Loader.js'
import { formatCodeResponse } from '/components/MonacoEditor.js'
import * as http from '/http.js'

export function createComponent(template) {
    const args = {
        template,
        created() {
            this.refresh();
        },
        data() {
            return {
                mode: 'list',
                list: null,
                name: null,
                refs: null,
                script: null,
                showRefs: false
            };
        },
        methods: {
            add() {
                this.mode = 'add';
                this.script = { name: '', code: '' };
            },
            assign(script) {
                http.put(`/api/keybinding-scripts-assign/${encodeURIComponent(script.name)}`, script.key).then(() => {
                    this.list.forEach(other => {
                        if (other != script && other.key == script.key) {
                            other.key = -1;
                        }
                    });
                }, this.handleError);
            },
            edit(name) {
                http.get(`/api/keybinding-scripts/${encodeURIComponent(name)}`).then(response => {
                    this.mode = 'edit';
                    this.name = name;
                    this.script = response;
                }, this.handleError);
            },
            refresh() {
                http.get('/api/keybinding-scripts').then(response => {
                    this.mode = 'list';
                    this.list = response;
                }, this.handleError);
            },
            remove(name) {
                http.delete(`/api/keybinding-scripts/${encodeURIComponent(name)}`).then(() => {
                    this.refresh();
                }, this.handleError);
            },
            save() {
                const onSaved = response => {
                    if (response.ok) {
                        this.refresh();
                    } else {
                        alert(formatCodeResponse(response));
                    }
                };
                if (this.mode == 'add') {
                    http.post('/api/keybinding-scripts', this.script).then(onSaved, this.handleError);
                } else if (this.mode == 'edit') {
                    http.put(`/api/keybinding-scripts/${encodeURIComponent(this.name)}`, this.script).then(onSaved, this.handleError);
                }
            },
            showApiRef() {
                if (this.showRefs) {
                    this.showRefs = false;
                } else if (this.refs) {
                    this.showRefs = true;
                } else {
                    http.get('/api/scripts-doc/KEYBINDING').then(response => {
                        this.showRefs = true;
                        this.refs = response;
                    }, this.handleError);
                }
            },
            handleError(error) {
                alert(error.response);
            }
        }
    };
    components.add(args, 'ScriptEditor');
    return withCss(import.meta.url, args);
}