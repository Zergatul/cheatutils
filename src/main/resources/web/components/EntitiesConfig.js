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
            axios.get('/api/entities').then(function (response) {
                self.entitiesConfigList = response.data;
                self.entitiesConfigMap = {};
                self.entitiesConfigList.forEach(c => self.entitiesConfigMap[c.clazz] = c);
            });
        },
        data() {
            return {
                state: 'list',
                search: '',
                entitiesList: null,
                entitiesMap: null,
                entitiesConfigList: null,
                entitiesConfigMap: null,
                selectedConfig: null
            };
        },
        methods: {
            backToList() {
                this.state = 'list';
            },
            filterEntityList() {
                let search = this.search.toLocaleLowerCase();
                this.entityListFiltered = this.entitiesList.filter(function (entity) {
                    if (entity.simpleName) {
                        let name = entity.simpleName.toLocaleLowerCase();
                        if (name.indexOf(search) >= 0) {
                            return true;
                        }
                    }
                    return false;
                });
            },
            openAdd() {
                this.state = 'add';
                this.search = '';
                this.filterEntityList();
            },
            openEdit(clazz) {
                this.state = 'edit';
                if (this.entitiesConfigMap[clazz]) {
                    this.selectedConfig = this.entitiesConfigMap[clazz];
                } else {
                    this.selectedConfig = null;
                    let self = this;
                    axios.post('/api/entities', { clazz: clazz }).then(function (response) {
                        self.selectedConfig = response.data;
                        self.entitiesConfigList.push(self.selectedConfig);
                        self.entitiesConfigMap[clazz] = self.selectedConfig;
                    });
                }
            },
            remove() {
                if (this.selectedConfig) {
                    var self = this;
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
            update(config) {
                axios.put('/api/entities/' + config.clazz, config);
            }
        }
    };
    addComponent(args, 'ColorBox');
    addComponent(args, 'ColorPicker');
    return args;
}

export { createComponent }