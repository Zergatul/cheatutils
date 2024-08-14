import * as http from '/http.js';

function createComponent(template) {
    return {
        template: template,
        created() {
            http.get('/api/monaco-editor-settings').then(response => {
                this.config = response;
            });
        },
        data() {
            return {
                config: null
            };
        },
        methods: {
            save() {
                if (this.config.json) {
                    try {
                        JSON.parse(this.config.json);
                    } catch (e) {
                        alert(e);
                        return;
                    }
                }
                http.post('/api/monaco-editor-settings', this.config).then(response => {
                    this.config = response;
                    this.$emit('back');
                });
            }
        }
    }
}

export { createComponent }