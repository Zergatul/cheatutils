import { components } from '../../components.js'
import { withCss } from '/components/Loader.js'
import { formatCodeResponse } from '/components/MonacoEditor.js'
import * as http from '/http.js'

export function createComponent(template) {
    const args = {
        template: template,
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
                this.script = {};
            },
            assign(script) {
                http.put(`/api/keybinding-scripts-assign/${encodeURIComponent(script.name)}`, script.key).then(response => {
                    this.list.forEach(s => {
                        if (s != script && s.key == script.key) {
                            s.key = -1;
                        }
                    })
                });
            },
            edit(name) {
                http.get(`/api/keybinding-scripts/${encodeURIComponent(name)}`).then(response => {
                    this.mode = 'edit';
                    this.name = name;
                    this.script = response;
                });
            },
            refresh() {
                http.get('/api/keybinding-scripts').then(response => {
                    this.mode = 'list';
                    this.list = response;
                });
            },
            remove(name) {
                http.delete(`/api/keybinding-scripts/${encodeURIComponent(name)}`).then(response => {
                    this.refresh();
                });
            },
            save() {
                let handleError = error => {
                    alert(error.response);
                }
                if (this.mode == 'add') {
                    http.post('/api/keybinding-scripts', this.script).then(response => {
                        if (response.ok) {
                            this.refresh();
                        } else {
                            alert(formatCodeResponse(response));
                        }
                    }, handleError);
                }
                if (this.mode == 'edit') {
                    http.put(`/api/keybinding-scripts/${encodeURIComponent(this.name)}`, this.script).then(response => {
                        if (response.ok) {
                            this.refresh();
                        } else {
                            alert(formatCodeResponse(response));
                        }
                    }, handleError);
                }
            },
            showApiRef() {
                if (this.showRefs) {
                    this.showRefs = false;
                } else {
                    if (this.refs) {
                        this.showRefs = true;
                    } else {
                        http.get('/api/scripts-doc/KEYBINDING').then(response => {
                            this.showRefs = true;
                            this.refs = response;
                        });
                    }
                }
            }
        }
    };
    components.add(args, 'ScriptEditor');
    components.add(args, 'SwitchCheckbox');
    return withCss(import.meta.url, args);
}