function createComponent(template) {
    return {
        template: template,
        created() {
            axios.get('/api/core').then(response => {
                this.config = response.data;
            });
        },
        data() {
            return {
                config: null
            };
        },
        methods: {
            update() {
                axios.post('/api/core', this.config).then(response => {
                    this.config = response.data;
                });
            }
        }
    }
}

export { createComponent }