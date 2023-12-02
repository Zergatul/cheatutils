import { addComponent } from '/components/Loader.js'

function createComponent(template) {
    let args = {
        template: template,
        created() {
            this.refresh();
        },
        data() {
            return {
                config: null,
                code: null,
                refs: null,
                showRefs: false
            };
        },
        methods: {
            refresh() {
                let self = this;
                axios.get('/api/events-scripting').then(response => {
                    self.config = response.data;
                    self.code = self.config.code;
                    delete self.config.code;
                });
            },
            save() {
                let self = this;
                axios.post('/api/events-scripting-code', this.code).then(() => {
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
                        axios.get('/api/scripts-doc/events').then(response => {
                            self.showRefs = true;
                            self.refs = response.data;
                        });
                    }
                }
            },
            update() {
                axios.post('/api/events-scripting', this.config);
            }
        }
    };
    addComponent(args, 'ScriptEditor');
    return args;
}

export { createComponent }