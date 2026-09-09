import * as http from '/http.js'
import { components } from '/components.js'

export function createComponent(template) {
    const args = {
        template: template,
        created() {
            http.get('/api/free-cam').then(response => {
                this.config = response;
            });
        },
        data() {
            return {
                config: null,
                path: null,
                time: 1000
            };
        },
        methods: {
            update() {
                http.post('/api/free-cam', this.config).then(response => {
                    this.config = response;
                });
            }
        }
    };
    components.add(args, 'Radio');
    components.add(args, 'SwitchCheckbox');
    return args;
}