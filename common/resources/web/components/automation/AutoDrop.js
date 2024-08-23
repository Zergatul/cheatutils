import { withCss } from '/components/Loader.js';
import { components } from '/components.js'
import * as http from '/http.js';

function createComponent(template) {
    let args = {
        template: template,
        created() {
            http.get('/api/auto-drop').then(response => {
                this.config = response;
            });
        },
        data() {
            return {
                config: null
            };
        },
        methods: {
            onChange() {
                http.post('/api/auto-drop', this.config).then(response => {
                    this.config = response;
                });
            }
        }
    };
    components.add(args, 'ItemList');
    withCss(import.meta.url, args);
    return args;
}

export { createComponent }