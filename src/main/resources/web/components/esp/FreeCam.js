import * as http from '/http.js'
import { getComponent } from '/components/Loader.js'

export function createComponent(template) {
    return {
        template,
        components: {
            Radio: getComponent('common/Radio'),
            SwitchCheckbox: getComponent('common/SwitchCheckbox')
        },
        created() {
            http.get('/api/free-cam').then(config => { this.config = config; }).catch(error => this.showError(error));
            this.refreshState();
            this.timer = setInterval(() => this.refreshState(), 1000);
        },
        beforeUnmount() { clearInterval(this.timer); },
        data() {
            return { config: null, state: { active: false, available: false }, busy: false, error: '', timer: null, pending: false };
        },
        methods: {
            showError(error) { this.error = error.response || error.message || String(error); },
            async refreshState() {
                if (this.pending || this.busy) return;
                this.pending = true;
                try { this.state = await http.get('/api/free-cam-state'); }
                catch (error) { this.showError(error); }
                finally { this.pending = false; }
            },
            async toggle() {
                this.busy = true;
                this.error = '';
                try { this.state = await http.post('/api/free-cam-state', this.state.active ? 'disable' : 'enable'); }
                catch (error) { this.showError(error); }
                finally { this.busy = false; }
            },
            async update() {
                this.error = '';
                for (const field of ['acceleration', 'maxSpeed', 'slowdownFactor']) {
                    if (!Number.isFinite(this.config[field])) {
                        this.error = 'Camera speed settings must be numbers.';
                        return;
                    }
                }
                this.busy = true;
                try { this.config = await http.post('/api/free-cam', this.config); }
                catch (error) { this.showError(error); }
                finally { this.busy = false; }
            }
        }
    };
}
