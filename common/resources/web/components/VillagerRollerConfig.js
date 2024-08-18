import { addComponent } from '/components/Loader.js'
import { handleCodeSave } from '/components/MonacoEditor.js'
import * as http from '/http.js';

function createComponent(template) {
    let args = {
        template: template,
        created() {
            this.refresh().then(() => {
                this.code = this.config.code || '';
            });
        },
        data() {
            return {
                code: '',
                config: null,
                refs: null,
                showRefs: false
            };
        },
        methods: {
            refresh() {
                return http.get('/api/villager-roller').then(response => {
                    this.config = response;
                });
            },
            save() {
                handleCodeSave('/api/villager-roller-code', this.code);
            },
            showApiRef() {
                if (this.showRefs) {
                    this.showRefs = false;
                } else {
                    if (this.refs) {
                        this.showRefs = true;
                    } else {
                        http.get('/api/scripts-doc/VILLAGER_ROLLER').then(response => {
                            this.showRefs = true;
                            this.refs = response;
                        });
                    }
                }
            },
            start() {
                http.post('/api/villager-roller-status', { start: true });
            },
            stop() {
                http.post('/api/villager-roller-status', { stop: true });
            }
        }
    };
    addComponent(args, 'ScriptEditor');
    return args;
}

export { createComponent }