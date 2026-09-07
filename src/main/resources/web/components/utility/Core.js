import * as http from '/http.js'

export function createComponent(template) {
    return {
        template,
        created() {
            http.get('/api/core').then(response => {
                this.config = response;
            }).catch(error => this.showError(error));
        },
        data() {
            return { config: null, saving: false, error: '', saved: false, portChanged: false };
        },
        methods: {
            showError(error) {
                this.error = error.response || error.message || String(error);
            },
            async update() {
                this.error = '';
                this.saved = false;
                this.portChanged = false;
                const port = Number(this.config.port);
                if (!Number.isInteger(port) || port < 1 || port > 65535) {
                    this.error = 'Enter a port from 1 to 65535.';
                    return;
                }
                this.saving = true;
                try {
                    const previous = await http.get('/api/core');
                    this.config = await http.post('/api/core', { ...this.config, port });
                    this.saved = true;
                    this.portChanged = previous.port != this.config.port;
                } catch (error) {
                    this.showError(error);
                } finally {
                    this.saving = false;
                }
            }
        }
    };
}
