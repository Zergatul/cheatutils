function createComponent(template) {
    return {
        template: template,
        created() {
            let self = this;
            axios.get('/api/auto-craft').then(response => {
                self.config = response.data;
                self.state = 'list'
            });
            axios.get('/api/item-info').then(response => {
                self.itemList = response.data.filter(i => i);
            });
        },
        data() {
            return {
                config: null,
                itemList: null,
                itemListFiltered: null,
                state: null,
                search: ''
            };
        },
        methods: {
            add(item) {
                this.config.items.push(item);
                this.update().then(() => {
                    this.backToList();
                })
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
            openAdd() {
                this.state = 'add';
                this.search = '';
                this.filterItemList();
            },
            remove(item) {
                this.config.items = this.config.items.filter(i => i != item);
                this.update();
            },
            update() {
                let self = this;
                return axios.post('/api/auto-craft', this.config).then(response => {
                    self.config = response.data;
                });
            }
        }
    }
}

export { createComponent }