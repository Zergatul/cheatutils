import { components } from '../../components.js'
import { handleCodeSave } from '/components/MonacoEditor.js'
import * as http from '/http.js'

export function createComponent(template) {
    const args = {
        template,
        created() {
            this.refresh();
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
                const slots = this.slots.trim().split(',').filter(value => value).map(value => parseInt(value));
                if (slots.some(value => isNaN(value))) {
                    this.onConfigLoaded();
                    alert('Invalid format');
                    return;
                }
                if (slots.some(value => value <= 0 || value >= 10)) {
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
                    this.code = response.code ?? '';
                    this.onConfigLoaded();
                }, this.handleError);
            },
            save() {
                handleCodeSave('/api/block-automation-code', this.code);
            },
            showApiRef() {
                if (this.showRefs) {
                    this.showRefs = false;
                } else if (this.refs) {
                    this.showRefs = true;
                } else {
                    http.get('/api/scripts-doc/BLOCK_AUTOMATION').then(response => {
                        this.showRefs = true;
                        this.refs = response;
                    }, this.handleError);
                }
            },
            update() {
                http.post('/api/block-automation', this.config).then(response => {
                    this.config = response;
                    this.onConfigLoaded();
                }, this.handleError);
            },
            handleError(error) {
                alert(error.response);
            }
        }
    };
    components.add(args, 'ScriptEditor');
    components.add(args, 'SwitchCheckbox');
    return args;
}
