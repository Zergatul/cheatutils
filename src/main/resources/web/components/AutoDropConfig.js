function createComponent(template) {
    return {
        template: template,
        created() {
            let self = this;
            this.loadConfig();
            axios.get('/api/item-info').then(function (response) {
                self.itemList = response.data.filter(i => i);
            });
        },
        data() {
            return {
                config: null,
                itemList: null,
                itemListFiltered: null,
                state: 'list',
                search: ''
            };
        },
        methods: {
            add(item) {
                let self = this;
                axios.post('/api/auto-drop', item).then(function (response) {
                    self.state = 'list';
                    self.loadConfig();
                });
            },
            backToList() {
                this.state = 'list';
            },
            filterItemList() {
                let search = this.search.toLocaleLowerCase();
                this.itemListFiltered = this.itemList.filter(function (item) {
                    return item.toLocaleLowerCase().indexOf(search) >= 0;
                });
            },
            loadConfig() {
                let self = this;
                axios.get('/api/auto-drop').then(function (response) {
                    self.config = response.data;
                });
            },
            openAdd() {
                this.state = 'add';
                this.search = '';
                this.filterItemList();
            },
            remove(item) {
                let self = this;
                axios.delete('/api/auto-drop/' + encodeURIComponent(item)).then(function (response) {
                    if (response.data) {
                        self.loadConfig();
                    }
                });
            },
            update() {
                let self = this;
                axios.post('/api/auto-drop', this.config).then(function (response) {
                    self.config = response.data;
                });
            }
        }
    }
}

export { createComponent }