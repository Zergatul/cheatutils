import * as http from '/http.js'
import { components } from '/components.js';

function createSimpleComponent(url, template) {
    const args = {
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
    components.add(args, 'SwitchCheckbox');
    return args;
}

export { createSimpleComponent }