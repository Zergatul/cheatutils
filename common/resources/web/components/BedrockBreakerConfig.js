function createComponent(template) {
    return {
        template: template,
        created() {
            axios.get('/api/bedrock-breaker').then(response => {
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
                axios.post('/api/bedrock-breaker', this.config).then(response => {
                    this.config = response.data;
                });
            }
        }
    }
}

export { createComponent }