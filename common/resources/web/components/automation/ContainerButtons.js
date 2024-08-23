import { components } from '/components.js'
import * as http from '/http.js';

export function createComponent(template) {
    const args = {
        template: template,
        created() {
            http.get('/api/container-buttons').then(response => {
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
                http.post('/api/container-buttons', this.config).then(response => {
                    this.config = response;
                });
            }
        }
    };
    components.add(args, 'SwitchCheckbox');
    components.add(args, 'ItemList');
    return args;
}