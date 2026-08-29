import * as http from '/http.js'

export function createComponent(template) {
    return {
        template,
        created() {
            this.load();
        },
        data() {
            return {
                loaded: false,
                loading: false,
                profiles: null,
                selectedProfile: null,
                newProfile: '',
                isValidName: false
            };
        },
        methods: {
            change() {
                this.loading = true;
                http.post('/api/profiles', {
                    command: 'change',
                    name: this.selectedProfile
                }).catch(error => {
                    alert(error.response);
                }).finally(() => {
                    this.loading = false;
                });
            },
            createCopy() {
                this.create('copy');
            },
            createNew() {
                this.create('new');
            },
            create(command) {
                this.loading = true;
                http.post('/api/profiles', {
                    command,
                    name: this.newProfile
                }).then(() => {
                    this.newProfile = '';
                    this.validate();
                    return this.load();
                }).catch(error => {
                    alert(error.response);
                }).finally(() => {
                    this.loading = false;
                });
            },
            remove() {
                this.loading = true;
                http.delete('/api/profiles/' + encodeURIComponent(this.selectedProfile)).then(() => {
                    return this.load();
                }).catch(error => {
                    alert(error.response);
                }).finally(() => {
                    this.loading = false;
                });
            },
            load() {
                this.loaded = false;
                return Promise.all([
                    http.get('/api/profiles/current'),
                    http.get('/api/profiles/list')
                ]).then(([current, profiles]) => {
                    this.selectedProfile = current;
                    this.profiles = profiles;
                    this.loaded = true;
                    this.validate();
                }).catch(error => {
                    alert(error.response);
                });
            },
            validate() {
                if (!this.newProfile || (this.profiles && this.profiles.includes(this.newProfile))) {
                    this.isValidName = false;
                    return;
                }

                const invalidChars = ['\\', '/', ':', '*', '?', '"', '<', '>', '|'];
                this.isValidName = ![...this.newProfile].some(character => invalidChars.includes(character));
            }
        }
    };
}