import { components } from '../../components.js'
import { handleCodeSave } from '/components/MonacoEditor.js'
import * as http from '/http.js';

const modes = {
    SCRIPT: 'SCRIPT',
    REFS: 'REFS',
    FONT: 'FONT'
};

export function createComponent(template) {
    const args = {
        template: template,
        created() {
            this.refresh();
        },
        data() {
            return {
                config: null,
                mode: modes.SCRIPT,
                refs: null,
                showRefs: false
            };
        },
        methods: {
            isScriptMode() {
                return this.mode == modes.SCRIPT;
            },
            isRefsMode() {
                return this.mode == modes.REFS;
            },
            isFontMode() {
                return this.mode == modes.FONT;
            },
            refresh() {
                http.get('/api/status-overlay').then(response => {
                    this.config = response;
                });
            },
            save() {
                handleCodeSave('/api/status-overlay-code', this.config.code);
            },
            showApiRef() {
                if (this.mode == modes.REFS) {
                    this.mode = modes.SCRIPT;
                } else {
                    if (this.refs) {
                        this.mode = modes.REFS;
                    } else {
                        http.get('/api/scripts-doc/OVERLAY').then(response => {
                            this.mode = modes.REFS;
                            this.refs = response;
                        });
                    }
                }
            },
            showFontConfig() {
                this.mode = modes.FONT;
            },
            backToScript() {
                this.mode = modes.SCRIPT;
            },
            update() {
                http.post('/api/status-overlay', this.config).then(response => {
                    this.config = response;
                });
            }
        }
    };
    components.add(args, 'ScriptEditor');
    components.add(args, 'SwitchCheckbox');
    components.add(args, 'FontPicker');
    return args;
}