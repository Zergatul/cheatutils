import { components } from '../../components.js'
import { handleCodeSavePut } from '../MonacoEditor.js'
import { withCss } from '/components/Loader.js'
import * as http from '/http.js'

function queueEntryRequest(entry, runRequest) {
    // we normally serve via localhost,
    // and we don't want to overcomplicate code to handle all possible race conditions
    entry.pendingPromise = (entry.pendingPromise ?? Promise.resolve())
        .then(runRequest)
        .catch(error => {
            alert(error?.response ?? String(error));
        });
}

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
                queueEntryRequest(entry, async () => {
                    const { name, keyIndex } = entry;
                    if (entry.isNew) {
                        const response = await http.post(`/api/keybindings`, {
                            name: name,
                            keyIndex: keyIndex,
                        });
                        if (response.ok) {
                            entry.originalName = name;
                            entry.isNew = false;
                        } else {
                            alert(response.error);
                            // TODO: focus input?
                        }
                    } else if (entry.name != entry.originalName) {
                        const response = await http.put(`/api/keybindings/${encodeURIComponent(entry.originalName)}`, {
                            name: name,
                            keyIndex: keyIndex,
                        });
                        if (response.ok) {
                            entry.originalName = name;
                        } else {
                            alert(response.error);
                            // TODO: focus input?
                        }
                    }
                });
            },
            addNew() {
                this.entries.push({
                    name: '',
                    keyIndex: -1,
                    isNew: true,
                });
            },
            assignToKeyIndex(entry) {
                queueEntryRequest(entry, async () => {
                    if (entry.isNew) {
                        return;
                    }
                    const { keyIndex } = entry;
                    const response = await http.put(`/api/keybindings/${encodeURIComponent(entry.originalName)}`, {
                        name: entry.originalName,
                        keyIndex: keyIndex,
                    });
                    if (response.ok) {
                        this.entries.forEach(e => {
                            if (e != entry && e.keyIndex == keyIndex) {
                                e.keyIndex = -1;
                            }
                        })
                    } else {
                        alert(response.error);
                        // TODO: focus input?
                    }
                });
            },
            editScript(entry) {
                queueEntryRequest(entry, async () => {
                    if (entry.isNew) {
                        alert('Enter/fix entry name before editing script');
                    } else {
                        const response = await http.get(`/api/keybindings/${encodeURIComponent(entry.originalName)}`);
                        this.mode = 'edit';
                        this.editingEntry = response;
                    }
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
                queueEntryRequest(entry, async () => {
                    if (entry.isNew) {
                        this.entries = this.entries.filter(e => e != entry);
                    } else {
                        const response = await http.delete(`/api/keybindings/${encodeURIComponent(entry.originalName)}`)
                        if (response.ok) {
                            this.entries = this.entries.filter(e => e != entry);
                        } else {
                            alert(response.error);
                        }
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