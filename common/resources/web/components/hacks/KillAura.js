import * as http from '/http.js'
import { handleCodeSave } from '/components/MonacoEditor.js'
import { components } from '../../components.js'
import { withCss } from '/components/Loader.js'

export function createComponent(template) {
    const args = {
        template: template,
        created() {
            http.get('/api/kill-aura').then(response => {
                this.config = response;
                this.code = response.code;
            });
            this.loadPriorityList();
        },
        data() {
            return {
                code: '',
                config: null,
                search: null,
                state: 'list',
                priorityList: null,
                priorityListFiltered: null,
                newCustomEntry: null,
                refs: null,
                showRefs: false
            };
        },
        methods: {
            addPriorityEntry(entry) {
                entry.enabled = false;
                this.config.priorities.push(entry);
                this.state = 'list';
                this.update();
            },
            createNewCustomEntry() {
                if (!this.newCustomEntry.name) {
                    alert('Entry Name is required.');
                    return;
                }
                if (!this.newCustomEntry.className) {
                    alert('Class Name is required.');
                    return;
                }
                http.get('/api/class-name/' + this.newCustomEntry.className).then(() => {
                    this.newCustomEntry.enabled = false;
                    this.config.priorities.push(this.newCustomEntry);
                    this.config.customEntries.push(this.newCustomEntry);
                    this.state = 'list';

                    this.update().then(() => this.loadPriorityList());
                }).catch(error => {
                    if (error.response && error.response.status == 404) {
                        alert(`Class with name "${this.newCustomEntry.className}" doesn't exist.`)
                    }
                });
            },
            deleteCustomEntry(entry) {
                this.config.customEntries = this.config.customEntries.filter(e => e.name != entry.name);
                this.update().then(() => {
                    this.loadPriorityList().then(() => {
                        this.filterPriorityList();
                    });
                });
            },
            entryInPrioritiesList(entry) {
                return this.config.priorities.some(e => e.name == entry.name);
            },
            filterPriorityList() {
                let search = this.search.toLocaleLowerCase();
                this.priorityListFiltered = this.priorityList.filter(entry => {
                    return entry.name.toLocaleLowerCase().indexOf(search) >= 0;
                });
            },
            loadPriorityList() {
                return http.get('/api/kill-aura-info').then(response => {
                    this.priorityList = response;
                });
            },
            moveDown(index) {
                if (index < this.config.priorities.length - 1) {
                    this.swapPriorities(index, index + 1);
                    this.update();
                }
            },
            moveUp(index) {
                if (index > 0) {
                    this.swapPriorities(index, index - 1);
                    this.update();
                }
            },
            openAddNew() {
                this.state = 'add';
                this.search = '';
                this.filterPriorityList();
            },
            openCreateNewCustomEntry() {
                this.newCustomEntry = {};
                this.state = 'create-custom';
            },
            removePriorityEntry(index) {
                this.config.priorities.splice(index, 1);
                this.update();
            },
            saveCode() {
                handleCodeSave('/api/kill-aura-code', this.code);
            },
            showApiRef() {
                if (this.showRefs) {
                    this.showRefs = false;
                } else {
                    if (this.refs) {
                        this.showRefs = true;
                    } else {
                        http.get('/api/scripts-doc/KILL_AURA').then(response => {
                            this.showRefs = true;
                            this.refs = response;
                        });
                    }
                }
            },
            swapPriorities(index1, index2) {
                let item = this.config.priorities[index1];
                this.config.priorities[index1] = this.config.priorities[index2];
                this.config.priorities[index2] = item;
            },
            update() {
                if (this.config.maxHorizontalAngle == '') {
                    this.config.maxHorizontalAngle = null;
                }
                if (this.config.maxVerticalAngle == '') {
                    this.config.maxVerticalAngle = null;
                }
                return http.post('/api/kill-aura', this.config).then(response => {
                    this.config = response;
                });
            }
        }
    };
    components.add(args, 'Radio');
    components.add(args, 'SwitchCheckbox');
    components.add(args, 'ScriptEditor');
    return withCss(import.meta.url, args);
}