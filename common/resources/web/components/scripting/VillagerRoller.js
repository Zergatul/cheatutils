import { handleCodeSave } from '/components/MonacoEditor.js'
import * as http from '/http.js'
import { components } from '../../components.js'

const modes = {
    SCRIPT: 'SCRIPT',
    REFS: 'REFS',
    SETTINGS: 'SETTINGS'
};

export function createComponent(template) {
    const args = {
        template: template,
        created() {
            this.refresh().then(() => {
                this.code = this.config.code || '';
            });
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
            isSettingsMode() {
                return this.mode == modes.SETTINGS;
            },
            refresh() {
                return http.get('/api/villager-roller').then(response => {
                    this.config = response;
                });
            },
            save() {
                handleCodeSave('/api/villager-roller-code', this.code);
            },
            showApiRef() {
                if (this.isRefsMode()) {
                    this.mode = modes.SCRIPT;
                } else {
                    if (this.refs) {
                        this.mode = modes.REFS;
                    } else {
                        http.get('/api/scripts-doc/VILLAGER_ROLLER').then(response => {
                            this.mode = modes.REFS;
                            this.refs = response;
                        });
                    }
                }
            },
            showSettings() {
                if (this.mode == modes.SETTINGS) {
                    this.mode = modes.SCRIPT;
                } else {
                    this.mode = modes.SETTINGS;
                }
                
            },
            start() {
                http.post('/api/villager-roller-status', { start: true });
            },
            stop() {
                http.post('/api/villager-roller-status', { stop: true });
            },
            update() {
                http.post('/api/villager-roller', this.config).then(response => {
                    this.config = response;
                });
            }
        }
    };
    components.add(args, 'SwitchCheckbox');
    components.add(args, 'ScriptEditor');
    return args;
}