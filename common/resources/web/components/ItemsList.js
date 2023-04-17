function createComponent(template) {
    return {
        template: template,
        props: ['modelValue'],
        emits: ['update:modelValue', 'change'],
        created() {
            let self = this;
            self.items = self.modelValue;
            axios.get('/api/item-info').then(response => {
                self.itemList = response.data.filter(i => i);
            });
        },
        data() {
            return {
                state: 'list',
                items: [],
                itemList: [],
                itemListFiltered: [],
                search: ''
            };
        },
        methods: {
            add(item) {
                if (this.items.indexOf(item) < 0) {
                    this.items.push(item);
                    this.$emit('update:modelValue', this.items);
                    this.$emit('change');
                }
                this.backToList();
            },
            backToList() {
                this.state = 'list';
            },
            filterItemList() {
                let search = this.search.toLocaleLowerCase();
                this.itemListFiltered = this.itemList.filter(item => {
                    return item.toLocaleLowerCase().indexOf(search) >= 0;
                });
            },
            openAdd() {
                this.state = 'add';
                this.search = '';
                this.filterItemList();
            },
            remove(item) {
                let index = this.items.indexOf(item);
                if (index >= 0) {
                    this.items.splice(index, 1);
                    this.$emit('update:modelValue', this.items);
                    this.$emit('change');
                }
            }
        },
        watch: {
            modelValue(value) {
                this.items = value || [];
            }
        }
    };
}

export { createComponent }