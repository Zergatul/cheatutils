function createComponent(template) {
    return {
        template: template,
        methods: {
            start() {
                axios.post('/api/world-download', 'begin:test');
            },
            stop() {
                axios.post('/api/world-download', 'end');
            }
        }
    }
}

export { createComponent }