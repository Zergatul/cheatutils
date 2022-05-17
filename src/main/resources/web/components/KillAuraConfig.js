function createComponent(template) {
    return {
        template: template,
        created() {
            let self = this;
            axios.get('/api/kill-aura').then(function (response) {
                self.config = response.data;
            });
            axios.get('/api/kill-aura-info').then(function (response) {
                self.priorityList = response.data;
            });
        },
        data() {
            return {
                config: null,
                search: null,
                state: 'list',
                priorityList: null,
                priorityListFiltered: null
            };
        },
        methods: {
            addPriorityEntry(name) {
                this.config.priorities.push(name);
                this.state = 'list';
                this.update();
            },
            filterPriorityList() {
                let search = this.search.toLocaleLowerCase();
                let self = this;
                this.priorityListFiltered = this.priorityList.filter(function (entry) {
                    if (self.config.priorities.filter(e => e == entry.name).length) {
                        return false;
                    }
                    return entry.name.indexOf(search) >= 0;
                });
            },
            openAddNew() {
                this.state = 'add';
                this.search = '';
                this.filterPriorityList();
            },
            removePriorityEntry(name) {
                let index = this.config.priorities.indexOf(name);
                if (index >= 0) {
                    this.config.priorities.splice(index, 1);
                }
                this.update();
            },
            update() {
                let self = this;
                axios.post('/api/kill-aura', this.config).then(function (response) {
                    self.config = response.data;
                });
            },

        }
    }
}

export { createComponent }