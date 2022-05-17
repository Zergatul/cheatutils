function createComponent(template) {
    return {
        template: template,
        created() {
            var self = this;
            axios.get('/api/auto-disconnect').then(function (response) {
                self.config = response.data;
            });
        },
        data() {
            return {
                config: null
            };
        },
        methods: {
            update() {
                axios.post('/api/auto-disconnect', this.config);
            }
        }
    }
}

export { createComponent }