function createComponent(template) {
    return {
        template: template,
        created() {
            let self = this;
            axios.get('/api/kill-aura').then(function (response) {
                self.config = response.data;
            });
            this.loadPriorityList();
        },
        data() {
            return {
                config: null,
                search: null,
                state: 'list',
                priorityList: null,
                priorityListFiltered: null,
                newCustomEntry: null
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
                let self = this;
                axios.get('/api/class-name/' + this.newCustomEntry.className).then(() => {
                    this.newCustomEntry.enabled = false;
                    this.config.priorities.push(this.newCustomEntry);
                    this.config.customEntries.push(this.newCustomEntry);
                    this.state = 'list';

                    let self = this;
                    this.update().then(() => self.loadPriorityList());
                }).catch(error => {
                    if (error.response && error.response.status == 404) {
                        alert(`Class with name "${self.newCustomEntry.className}" doesn't exist.`)
                    }
                });
            },
            deleteCustomEntry(entry) {
                let self = this;
                this.config.customEntries = this.config.customEntries.filter(e => e.name != entry.name);
                this.update().then(() => {
                    self.loadPriorityList().then(() => {
                        self.filterPriorityList();
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
                let self = this;
                return axios.get('/api/kill-aura-info').then(response => {
                    self.priorityList = response.data;
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
            swapPriorities(index1, index2) {
                let item = this.config.priorities[index1];
                this.config.priorities[index1] = this.config.priorities[index2];
                this.config.priorities[index2] = item;
            },
            update() {
                let self = this;
                if (this.config.maxHorizontalAngle == '') {
                    this.config.maxHorizontalAngle = null;
                }
                if (this.config.maxVerticalAngle == '') {
                    this.config.maxVerticalAngle = null;
                }
                return axios.post('/api/kill-aura', this.config).then(response => {
                    self.config = response.data;
                });
            }
        }
    }
}

export { createComponent }