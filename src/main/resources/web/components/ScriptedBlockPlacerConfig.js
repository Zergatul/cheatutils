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
            onConfigLoaded() {
                this.slots = this.config.autoSelectSlots.join(',');
            },
            onSlotsUpdate() {
                let slots = this.slots.trim().split(',').filter(s => s).map(s => parseInt(s));
                if (slots.some(i => isNaN(i))) {
                    this.onConfigLoaded();
                    alert('Invalid format');
                    return;
                }
                if (slots.some(i => i <= 0 || i >= 10)) {
                    this.onConfigLoaded();
                    alert('Slot number out of range. Use 1..9');
                    return;
                }
                this.config.autoSelectSlots = slots;
                this.update();
            },
            refresh() {
                let self = this;
                return axios.get('/api/scripted-block-placer').then(response => {
                    self.config = response.data;
                    self.onConfigLoaded();
                });
            },
            save() {
                let self = this;
                axios.post('/api/scripted-block-placer-code', this.code).then(() => {
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
                        axios.get('/api/scripts-doc/block-placer').then(response => {
                            self.showRefs = true;
                            self.refs = response.data;
                        });
                    }
                }
            },
            update() {
                let self = this;
                axios.post('/api/scripted-block-placer', this.config).then(response => {
                    self.config = response.data;
                    self.onConfigLoaded();
                });
            }
        }
    };
    addComponent(args, 'ScriptEditor');
    return args;
}

export { createComponent }