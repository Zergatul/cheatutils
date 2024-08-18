import { addComponent } from '/components/Loader.js'
import { formatCodeResponse } from '/components/MonacoEditor.js'
import * as http from '/http.js';

let entityInfoPromise = http.get('/api/entity-info').then(entitiesList => {
    let entitiesMap = {};
    entitiesList.forEach(e => entitiesMap[e.clazz] = e);
    return {
        entitiesList: entitiesList,
        entitiesMap: entitiesMap
    }
});

function createComponent(template) {
    let args = {
        template: template,
        created() {
            entityInfoPromise.then(info => {
                this.entitiesList = info.entitiesList;
                this.entitiesMap = info.entitiesMap;
            });
            this.loadEntityConfigs();
        },
        data() {
            return {
                state: 'list',
                search: '',
                entitiesList: null,
                entitiesMap: null,
                entitiesConfigList: null,
                entitiesConfigMap: null,
                selectedConfig: null,
                entityListFiltered: null,
                code: null,
                refs: null,
                showRefs: false
            };
        },
        methods: {
            backToList() {
                this.state = 'list';
            },
            filterEntityList() {
                let search = this.search.toLocaleLowerCase();
                if (search == '') {
                    this.entityListFiltered = this.entitiesList.slice(0);
                    return;
                }

                let entries = [];
                this.entitiesList.forEach(entity => {
                    if (entity.simpleName) {
                        let name = entity.simpleName.toLocaleLowerCase();
                        let index = name.indexOf(search);
                        if (index >= 0) {
                            entries.push({
                                info: entity,
                                priority: index == 0 ? 100 : 99
                            });
                            return;
                        }
                    }
                    if (entity.id) {
                        let index = entity.id.indexOf(search);
                        if (index >= 0) {
                            entries.push({
                                info: entity,
                                priority: index == 0 ? 90 : 89
                            });
                            return;
                        }
                    }
                    if (entity.baseClasses) {
                        for (let i = 0; i < entity.baseClasses.length; i++) {
                            let index = entity.baseClasses[i].toLocaleLowerCase().indexOf(search);
                            if (index >= 0) {
                                entries.push({
                                    info: entity,
                                    priority: index == 0 ? 80 : 79
                                });
                                return;
                            }
                        }
                    }
                    if (entity.interfaces) {
                        for (let i = 0; i < entity.interfaces.length; i++) {
                            let index = entity.interfaces[i].toLocaleLowerCase().indexOf(search);
                            if (index >= 0) {
                                entries.push({
                                    info: entity,
                                    priority: index == 0 ? 70 : 69
                                });
                                return;
                            }
                        }
                    }
                });

                this.entityListFiltered = entries.sort((e1, e2) => e2.priority - e1.priority).map(e => e.info);
            },
            loadEntityConfigs() {
                http.get('/api/entities').then(response => {
                    this.entitiesConfigList = response;
                    this.entitiesConfigMap = {};
                    this.entitiesConfigList.forEach(c => this.entitiesConfigMap[c.clazz] = c);
                });
            },
            moveDown(config) {
                http.post('/api/entities-move', {
                    direction: 'down',
                    clazz: config.clazz
                }).then(response => {
                    response = response;
                    if (!response.ok) {
                        alert(response.message);
                    } else {
                        this.loadEntityConfigs();
                    }
                });
            },
            moveUp(config) {
                http.post('/api/entities-move', {
                    direction: 'up',
                    clazz: config.clazz
                }).then(response => {
                    if (!response.ok) {
                        alert(response.message);
                    } else {
                        this.loadEntityConfigs();
                    }
                });
            },
            openAdd() {
                this.state = 'add';
                this.search = '';
                this.filterEntityList();
            },
            openEdit(clazz) {
                this.state = 'edit';
                let setupCode = () => {
                    this.code = this.selectedConfig.code || '';
                    this.showRefs = false;
                };
                if (this.entitiesConfigMap[clazz]) {
                    this.selectedConfig = this.entitiesConfigMap[clazz];
                    setupCode();
                } else {
                    this.selectedConfig = null;
                    http.post('/api/entities', { clazz: clazz }).then(response => {
                        this.selectedConfig = response;
                        this.entitiesConfigList.push(this.selectedConfig);
                        this.entitiesConfigMap[clazz] = this.selectedConfig;
                        setupCode();
                    });
                }
            },
            remove() {
                if (this.selectedConfig) {
                    http.delete('/api/entities/' + this.selectedConfig.clazz).then(() => {
                        let clazz = this.selectedConfig.clazz;
                        let index = this.entitiesConfigList.indexOf(this.selectedConfig);
                        if (index >= 0) {
                            this.entitiesConfigList.splice(index, 1);
                        }
                        this.selectedConfig = null;
                        delete this.entitiesConfigMap[clazz];
                        this.backToList();
                    });
                }
            },
            removeByClass(clazz) {
                http.delete('/api/entities/' + clazz).then(() => {
                    let index = this.entitiesConfigList.findIndex(e => e.clazz == clazz);
                    if (index >= 0) {
                        this.entitiesConfigList.splice(index, 1);
                    }
                    delete this.entitiesConfigMap[clazz];
                });
            },
            update(config) {
                if (config.tracerMaxDistance == '') {
                    config.tracerMaxDistance = null;
                }
                if (config.glowMaxDistance == '') {
                    config.glowMaxDistance = null;
                }
                if (config.outlineMaxDistance == '') {
                    config.outlineMaxDistance = null;
                }
                http.put('/api/entities/' + config.clazz, config);
            },
            showApiRef() {
                if (this.showRefs) {
                    this.showRefs = false;
                } else {
                    if (this.refs) {
                        this.showRefs = true;
                    } else {
                        http.get('/api/scripts-doc/ENTITY_ESP').then(response => {
                            this.showRefs = true;
                            this.refs = response;
                        });
                    }
                }
            },
            saveCode() {
                http.post('/api/entity-esp-code', {
                    clazz: this.selectedConfig.clazz,
                    code: this.code
                }).then(response => {
                    if (response.ok) {
                        alert('Saved');
                    } else {
                        alert(formatCodeResponse(response));
                    }
                }, error => {
                    alert(error.response);
                });
            }
        }
    };
    addComponent(args, 'ColorBox');
    addComponent(args, 'ColorPicker');
    addComponent(args, 'ScriptEditor');
    return args;
}

export { createComponent }