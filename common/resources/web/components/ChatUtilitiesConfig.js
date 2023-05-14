function createComponent(template) {
    return {
        template: template,
        created() {
            let self = this;
            axios.get('/api/chat-utilities').then(function (response) {
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
                let self = this;
                axios.post('/api/chat-utilities', this.config).then(function (response) {
                    self.config = response.data;
                });
            }
        }
    }
}

export { createComponent }