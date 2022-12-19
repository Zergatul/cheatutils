function createComponent(template) {
    return {
        template: template,
        created() {
            let self = this;
            axios.get('/api/exploration-mini-map').then(function (response) {
                self.config = response.data;
            });
        },
        data() {
            return {
                config: null
            };
        },
        methods: {
            addMarker() {
                axios.post('/api/exploration-mini-map-markers', {});
            },
            clearMarkers() {
                axios.delete('/api/exploration-mini-map-markers/all', {});
            },
            update() {
                let self = this;
                if (this.config.scanFromY == '') {
                    this.config.scanFromY = null;
                }
                axios.post('/api/exploration-mini-map', this.config).then(function (response) {
                    self.config = response.data;
                });
            }
        }
    }
}

export { createComponent }