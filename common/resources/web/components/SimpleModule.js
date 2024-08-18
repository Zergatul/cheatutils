import * as http from '/http.js';

function createSimpleComponent(url, template) {
    return {
        template: template,
        created() {
            http.get(url).then(response => {
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
                http.post(url, this.config).then(response => {
                    this.config = response;
                });
            }
        }
    };
}

export { createSimpleComponent }