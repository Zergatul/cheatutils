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
            moveDown(index) {
                if (index < this.config.priorities.length - 1) {
                    this.swapPriorities(index, index - 1);
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
            removePriorityEntry(index) {
                this.config.priorities.splice(index, 1);
                this.update();
            },
            swapPriorities(index1, index2) {
                let item = this.config.priorities[index1];
                this.config.priorities[index1] = this.config.priorities[index2];
                this.config.priorities[index2] = item;
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