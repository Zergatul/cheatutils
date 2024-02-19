import { addComponent } from '/components/Loader.js'

let entityInfoPromise = axios.get('/api/entity-info').then(function (response) {
    let entitiesList = response.data;
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
            let self = this;
            entityInfoPromise.then(function (info) {
                self.entitiesList = info.entitiesList;
                self.entitiesMap = info.entitiesMap;
            });
            self.loadEntityConfigs();
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
                let self = this;
                axios.get('/api/entities').then(function (response) {
                    self.entitiesConfigList = response.data;
                    self.entitiesConfigMap = {};
                    self.entitiesConfigList.forEach(c => self.entitiesConfigMap[c.clazz] = c);
                });
            },
            moveDown(config) {
                let self = this;
                axios.post('/api/entities-move', {
                    direction: 'down',
                    clazz: config.clazz
                }).then(response => {
                    response = response.data;
                    if (!response.ok) {
                        alert(response.message);
                    } else {
                        self.loadEntityConfigs();
                    }
                });
            },
            moveUp(config) {
                let self = this;
                axios.post('/api/entities-move', {
                    direction: 'up',
                    clazz: config.clazz
                }).then(response => {
                    response = response.data;
                    if (!response.ok) {
                        alert(response.message);
                    } else {
                        self.loadEntityConfigs();
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
                    axios.post('/api/entities', { clazz: clazz }).then(response => {
                        this.selectedConfig = response.data;
                        this.entitiesConfigList.push(this.selectedConfig);
                        this.entitiesConfigMap[clazz] = this.selectedConfig;
                        setupCode();
                    });
                }
            },
            remove() {
                if (this.selectedConfig) {
                    let self = this;
                    axios.delete('/api/entities/' + this.selectedConfig.clazz).then(function (response) {
                        let clazz = self.selectedConfig.clazz;
                        let index = self.entitiesConfigList.indexOf(self.selectedConfig);
                        if (index >= 0) {
                            self.entitiesConfigList.splice(index, 1);
                        }
                        self.selectedConfig = null;
                        delete self.entitiesConfigMap[clazz];
                        self.backToList();
                    });
                }
            },
            removeByClass(clazz) {
                let self = this;
                axios.delete('/api/entities/' + clazz).then(function (response) {
                    let index = self.entitiesConfigList.findIndex(e => e.clazz == clazz);
                    if (index >= 0) {
                        self.entitiesConfigList.splice(index, 1);
                    }
                    delete self.entitiesConfigMap[clazz];
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
                axios.put('/api/entities/' + config.clazz, config);
            },
            showApiRef() {
                if (this.showRefs) {
                    this.showRefs = false;
                } else {
                    if (this.refs) {
                        this.showRefs = true;
                    } else {
                        let self = this;
                        axios.get('/api/scripts-doc/entity-esp').then(response => {
                            self.showRefs = true;
                            self.refs = response.data;
                        });
                    }
                }
            },
            saveCode() {
                axios.post('/api/entity-esp-code', {
                    clazz: this.selectedConfig.clazz,
                    code: this.code
                }).then(() => {
                    alert('Saved');
                }, error => {
                    alert(error.response.data);
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