import * as http from '/http.js';

function createComponent(template) {
    return {
        template: template,
        created() {
            http.get('/api/free-cam').then(response => {
                this.config = response;
            });
            this.reloadPath();
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
                http.post('/api/free-cam-path', this.time).then(response => {
                    this.reloadPath();
                });
            },
            clearPath() {
                http.delete('/api/free-cam-path/_').then(response => {
                    this.reloadPath();
                });
            },
            reloadPath() {
                http.get('/api/free-cam-path').then(response => {
                    this.path = response;
                });
            },
            update() {
                let self = this;
                http.post('/api/free-cam', this.config).then(response => {
                    this.config = response;
                });
            }
        }
    }
}

export { createComponent }