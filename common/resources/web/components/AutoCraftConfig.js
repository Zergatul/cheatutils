import * as http from '/http.js';

function createComponent(template) {
    return {
        template: template,
        created() {
            http.get('/api/auto-craft').then(response => {
                this.config = response;
                this.state = 'list'
            });
            http.get('/api/item-info').then(response => {
                this.itemList = response.filter(i => i);
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
                return http.post('/api/auto-craft', this.config).then(response => {
                    this.config = response;
                });
            }
        }
    }
}

export { createComponent }