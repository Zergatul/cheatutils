import { handleCodeSave } from '/components/MonacoEditor.js'
import * as http from '/http.js'
import { components } from '../../components.js'

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
                mode: modes.SCRIPT,
                code: '',
                config: null,
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
                return http.get('/api/villager-roller').then(response => {
                    this.config = response;
                    this.code = response.code ?? '';
                }, this.handleError);
            },
            save() {
                handleCodeSave('/api/villager-roller-code', this.code);
            },
            showApiRef() {
                if (this.isRefsMode()) {
                    this.mode = modes.SCRIPT;
                } else if (this.refs) {
                    this.mode = modes.REFS;
                } else {
                    http.get('/api/scripts-doc/VILLAGER_ROLLER').then(response => {
                        this.mode = modes.REFS;
                        this.refs = response;
                    }, this.handleError);
                }
            },
            start() {
                http.post('/api/villager-roller-status', { start: true }).catch(this.handleError);
            },
            stop() {
                http.post('/api/villager-roller-status', { stop: true }).catch(this.handleError);
            },
            handleError(error) {
                alert(error.response);
            }
        }
    };
    components.add(args, 'ScriptEditor');
    return args;
}