import { components } from '../../components.js'
import { handleCodeSavePut } from '../MonacoEditor.js'
import { withCss } from '/components/Loader.js'
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
                entries: null,
                editingEntry: null,
                refs: null,
                showRefs: false,
            };
        },
        methods: {
            onEntryNameBlur(entry) {
                if (entry.isNew) {
                    http.post(`/api/keybindings`, {
                        name: entry.name,
                        keyIndex: entry.keyIndex,
                    }).then(response => {
                        if (response.ok) {
                            entry.originalName = entry.name;
                            entry.isNew = false;
                        } else {
                            alert(response.error);
                            // TODO: focus input?
                        }
                    });
                } else if (entry.name != entry.originalName) {
                    http.put(`/api/keybindings/${encodeURIComponent(entry.originalName)}`, {
                        name: entry.name,
                        keyIndex: entry.keyIndex,
                    }).then(response => {
                        if (response.ok) {
                            entry.originalName = entry.name;
                        } else {
                            alert(response.error);
                            // TODO: focus input?
                        }
                    });
                }
            },
            addNew() {
                this.entries.push({
                    name: '',
                    keyIndex: -1,
                    isNew: true,
                });
            },
            assignToKeyIndex(entry) {
                if (entry.isNew) {
                    return;
                }
                http.put(`/api/keybindings/${encodeURIComponent(entry.name)}`, {
                    name: entry.name,
                    keyIndex: entry.keyIndex,
                }).then(response => {
                    if (response.ok) {
                        this.entries.forEach(e => {
                            if (e != entry && e.keyIndex == entry.keyIndex) {
                                e.keyIndex = -1;
                            }
                        })
                    } else {
                        alert(response.error);
                        // TODO: focus input?
                    }
                });
            },
            editScript(name) {
                http.get(`/api/keybindings/${encodeURIComponent(name)}`).then(response => {
                    this.mode = 'edit';
                    this.editingEntry = response;
                });
            },
            refresh() {
                http.get('/api/keybindings').then(response => {
                    this.mode = 'list';
                    this.entries = response.map(e => {
                        return {
                            name: e.name,
                            keyIndex: e.keyIndex,
                            originalName: e.name,
                        };
                    });
                });
            },
            removeEntry(entry) {
                http.delete(`/api/keybindings/${encodeURIComponent(entry.name)}`).then(response => {
                    if (response.ok) {
                        this.entries = this.entries.filter(e => e.name != entry.name);
                    } else {
                        alert(response.error);
                    }
                });
            },
            saveScript() {
                handleCodeSavePut(`/api/keybindings-code/${encodeURIComponent(this.editingEntry.name)}`, this.editingEntry.code);
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