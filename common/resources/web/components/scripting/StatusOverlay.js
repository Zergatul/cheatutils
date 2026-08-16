import { components } from '../../components.js'
import { handleCodeSave } from '/components/MonacoEditor.js'
import * as http from '/http.js'

const modes = {
    SCRIPT: 'SCRIPT',
    REFS: 'REFS'
};

export function createComponent(template) {
    const args = {
        template,
        created() {
            this.refresh();
        },
        data() {
            return {
                config: null,
                mode: modes.SCRIPT,
                refs: null
            };
        },
        methods: {
            isScriptMode() {
                return this.mode == modes.SCRIPT;
            },
            isRefsMode() {
                return this.mode == modes.REFS;
            },
            refresh() {
                http.get('/api/status-overlay').then(response => {
                    response.code ??= '';
                    this.config = response;
                }, this.handleError);
            },
            save() {
                handleCodeSave('/api/status-overlay-code', this.config.code);
            },
            showApiRef() {
                if (this.mode == modes.REFS) {
                    this.mode = modes.SCRIPT;
                } else if (this.refs) {
                    this.mode = modes.REFS;
                } else {
                    http.get('/api/scripts-doc/OVERLAY').then(response => {
                        this.mode = modes.REFS;
                        this.refs = response;
                    }, this.handleError);
                }
            },
            update() {
                http.post('/api/status-overlay', this.config).then(response => {
                    response.code ??= '';
                    this.config = response;
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