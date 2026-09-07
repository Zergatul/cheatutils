import * as http from '/http.js'

export function createComponent(template) {
    return {
        template,
        created() { this.load().catch(error => this.showError(error)); },
        data() {
            return { loaded: false, profiles: [], selectedProfile: '', newProfile: '', isValidName: false, busy: false, error: '' };
        },
        methods: {
            showError(error) { this.error = error.response || error.message || String(error); },
            async perform(action) {
                this.busy = true;
                this.error = '';
                try {
                    await action();
                    await this.load();
                } catch (error) {
                    this.showError(error);
                } finally {
                    this.busy = false;
                }
            },
            change() { return this.perform(() => http.post('/api/profiles', { command: 'change', name: this.selectedProfile })); },
            createCopy() { return this.perform(() => http.post('/api/profiles', { command: 'copy', name: this.newProfile })); },
            createNew() { return this.perform(() => http.post('/api/profiles', { command: 'new', name: this.newProfile })); },
            remove() { return this.perform(() => http.delete('/api/profiles/' + encodeURIComponent(this.selectedProfile))); },
            async load() {
                const [current, profiles] = await Promise.all([http.get('/api/profiles/current'), http.get('/api/profiles/list')]);
                this.selectedProfile = current;
                this.profiles = profiles;
                this.loaded = true;
                this.validate();
            },
            validate() {
                const name = this.newProfile;
                this.isValidName = name.trim().length > 0 && name.length <= 100 &&
                    !/[\\/:*?"<>|\x00-\x1f]/.test(name) && !/[. ]$/.test(name) &&
                    !this.profiles.some(profile => profile.toLowerCase() == name.toLowerCase());
            }
        }
    };
}
