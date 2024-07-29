import { addComponent } from '/components/Loader.js'
import { handleCodeSave } from '/components/MonacoEditor.js'

function createComponent(template) {
    let args = {
        template: template,
        created() {
            let self = this;
            this.refresh().then(() => {
                self.code = self.config.code || '';
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
                let self = this;
                return axios.get('/api/villager-roller').then(response => {
                    self.config = response.data;
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
                        let self = this;
                        axios.get('/api/scripts-doc/villager-roller').then(response => {
                            self.showRefs = true;
                            self.refs = response.data;
                        });
                    }
                }
            },
            start() {
                axios.post('/api/villager-roller-status', { start: true });
            },
            stop() {
                axios.post('/api/villager-roller-status', { stop: true });
            }
        }
    };
    addComponent(args, 'ScriptEditor');
    return args;
}

export { createComponent }