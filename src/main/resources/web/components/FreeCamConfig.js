function createComponent(template) {
    return {
        template: template,
        created() {
            let self = this;
            axios.get('/api/free-cam').then(response => {
                self.config = response.data;
            });
            self.reloadPath();
        },
        data() {
            return {
                config: null,
                path: null,
                time: 1000
            };
        },
        methods: {
            addPathPoint() {
                let self = this;
                axios.post('/api/free-cam-path', self.time).then(response => {
                    self.reloadPath();
                });
            },
            clearPath() {
                let self = this;
                axios.delete('/api/free-cam-path/_').then(response => {
                    self.reloadPath();
                });
            },
            reloadPath() {
                let self = this;
                axios.get('/api/free-cam-path').then(response => {
                    self.path = response.data;
                });
            },
            update() {
                let self = this;
                axios.post('/api/free-cam', this.config).then(function (response) {
                    self.config = response.data;
                });
            }
        }
    }
}

export { createComponent }