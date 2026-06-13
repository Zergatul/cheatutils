import { components } from '/components.js'
import * as http from '/http.js'
import { withCss } from '/components/Loader.js'

export function createComponent(template) {
    const url = '/api/crystal-aura';
    const args = {
        template: template,
        created() {
            http.get(url).then(response => {
                this.config = response;
            });
        },
        data() {
            return {
                config: null,
                showAddNew: false
            };
        },
        methods: {
            showAutoComplete() {
                this.showAddNew = true;
            },
            removeTarget(index) {
                this.config.targets.splice(index, 1);
                this.update();
            },
            update() {
                http.post(url, this.config).then(response => {
                    this.config = response;
                });
            }
        }
    };
    components.add(args, 'SwitchCheckbox');
    components.add(args, 'AutoComplete');
    components.add(args, 'Description');
    return withCss(import.meta.url, args);
}