import { addComponent } from '/components/Loader.js'

function createComponent(template) {
    let args = {
        template: template,
        created() {
            let self = this;
            this.refresh().then(() => {
                self.code = self.config.code || '';
            });
        },
        data() {
            return {
                code: '',
                config: null,
                refs: null,
                showRefs: false,
                slots: null
            };
        },
        methods: {
            refresh() {
                let self = this;
                return axios.get('/api/auto-disconnect').then(response => {
                    self.config = response.data;
                });
            },
            save() {
                let self = this;
                axios.post('/api/auto-disconnect-code', this.code).then(() => {
                    alert('Saved');
                }, error => {
                    alert(error.response.data);
                });
            },
            showApiRef() {
                if (this.showRefs) {
                    this.showRefs = false;
                } else {
                    if (this.refs) {
                        this.showRefs = true;
                    } else {
                        let self = this;
                        axios.get('/api/scripts-doc/auto-disconnect').then(response => {
                            self.showRefs = true;
                            self.refs = response.data;
                        });
                    }
                }
            },
            update() {
                let self = this;
                axios.post('/api/auto-disconnect', this.config).then(response => {
                    self.config = response.data;
                });
            }
        }
    };

    addComponent(args, 'ScriptEditor');
    return args;
}

export { createComponent }