import * as http from '/http.js'
import { withCss } from '/components/Loader.js'
import { components } from '/components.js'

export function createSimpleComponent(url, template, settings) {
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
    if (settings) {
        if (settings.components) {
            for (let component of settings.components) {
                components.add(args, component);
            }
        }
        if (settings.css) {
            return withCss(settings.css, args);
        }
    }
    return args;
}