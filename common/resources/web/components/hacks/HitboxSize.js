import * as http from '/http.js'
import { components } from '../../components.js'

export function createComponent(template) {
    const args = {
        template: template,
        created() {
            http.get('/api/hitbox-size').then(response => {
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
                return http.post('/api/hitbox-size', this.config).then(response => {
                    this.config = response;
                });
            }
        }
    };
    components.add(args, 'Radio');
    components.add(args, 'SwitchCheckbox');
    return args;
}