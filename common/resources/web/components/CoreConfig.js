import * as http from '/http.js';

function createComponent(template) {
    return {
        template: template,
        created() {
            http.get('/api/core').then(response => {
                this.config = response;
            });
        },
        data() {
            return {
                config: null
            };
        },
        methods: {
            update() {
                http.post('/api/core', this.config).then(response => {
                    this.config = response;
                    if (location.port != this.config.port) {
                        setTimeout(() => {
                            location.assign(location.protocol + '//' + location.hostname + ':' + this.config.port + '/');
                        }, 500);
                    }
                });
            }
        }
    }
}

export { createComponent }