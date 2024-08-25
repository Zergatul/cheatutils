import { components } from '../../components.js'
import { handleCodeSave } from '/components/MonacoEditor.js'
import * as http from '/http.js'

export function createComponent(template) {
    const args = {
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
                showRefs: false,
                slots: null
            };
        },
        methods: {
            onConfigLoaded() {
                this.slots = this.config.autoSelectSlots.join(',');
            },
            onSlotsUpdate() {
                let slots = this.slots.trim().split(',').filter(s => s).map(s => parseInt(s));
                if (slots.some(i => isNaN(i))) {
                    this.onConfigLoaded();
                    alert('Invalid format');
                    return;
                }
                if (slots.some(i => i <= 0 || i >= 10)) {
                    this.onConfigLoaded();
                    alert('Slot number out of range. Use 1..9');
                    return;
                }
                this.config.autoSelectSlots = slots;
                this.update();
            },
            refresh() {
                return http.get('/api/block-automation').then(response => {
                    this.config = response;
                    this.onConfigLoaded();
                });
            },
            save() {
                handleCodeSave('/api/block-automation-code', this.code);
            },
            showApiRef() {
                if (this.showRefs) {
                    this.showRefs = false;
                } else {
                    if (this.refs) {
                        this.showRefs = true;
                    } else {
                        http.get('/api/scripts-doc/BLOCK_AUTOMATION').then(response => {
                            this.showRefs = true;
                            this.refs = response;
                        });
                    }
                }
            },
            update() {
                http.post('/api/block-automation', this.config).then(response => {
                    this.config = response;
                    this.onConfigLoaded();
                });
            }
        }
    };
    components.add(args, 'ScriptEditor');
    components.add(args, 'SwitchCheckbox');
    return args;
}