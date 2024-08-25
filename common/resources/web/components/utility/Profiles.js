export function createComponent(template) {
    const args = {
        template: template,
        created() {
            this.load();
        },
        data() {
            return {
                loaded: false,
                profiles: null,
                selectedProfile: null,
                newProfile: '',
                isValidName: false
            };
        },
        methods: {
            change() {
                fetch('/api/profiles', {
                    method: 'POST',
                    body: JSON.stringify({
                        command: 'change',
                        name: this.selectedProfile
                    })
                });
            },
            createCopy() {
                fetch('/api/profiles', {
                    method: 'POST',
                    body: JSON.stringify({
                        command: 'copy',
                        name: this.newProfile
                    })
                }).then(() => {
                    this.load();
                });
            },
            createNew() {
                fetch('/api/profiles', {
                    method: 'POST',
                    body: JSON.stringify({
                        command: 'new',
                        name: this.newProfile
                    })
                }).then(() => {
                    this.load();
                });
            },
            remove() {
                fetch('/api/profiles/' + encodeURIComponent(this.selectedProfile), {
                    method: 'DELETE'
                }).then(() => {
                    this.load();
                });
            },
            load() {
                fetch('/api/profiles/current').then(async response => {
                    this.selectedProfile = await response.json();
                    this.setLoaded();
                });
                fetch('/api/profiles/list').then(async response => {
                    this.profiles = await response.json();
                    this.setLoaded();
                });
            },
            setLoaded() {
                this.loaded = this.selectedProfile != null && this.profiles != null;
            },
            validate() {
                if (this.newProfile.length == 0) {
                    this.isValidName = false;
                    return;
                }

                const invalidChars = ['\\', '/', ':', '*', '?', '"', '<', '>', '|'];
                for (let ch1 of this.newProfile) {
                    for (let ch2 of invalidChars) {
                        if (ch1 == ch2) {
                            this.isValidName = false;
                            return;
                        }
                    }
                }

                this.isValidName = true;
            }
        }
    };
    return args;
}