function createComponent(template) {
    return {
        template: template,
        created() {
            this.refresh();
        },
        data() {
            return {
                config: null,
                code: null,
                refs: null
            };
        },
        methods: {
            refresh() {
                let self = this;
                axios.get('/api/game-tick-scripting').then(response => {
                    self.config = response.data;
                    self.code = self.config.code;
                    delete self.config.code;
                });
            },
            save() {
                let self = this;
                axios.post('/api/game-tick-scripting-code', this.code).then(() => {
                    alert('Saved');
                }, error => {
                    alert(error.response.data);
                });
            },
            showApiRef() {
                if (this.refs) {
                    this.refs = null;
                } else {
                    let self = this;
                    axios.get('/api/scripts-doc/keys').then(response => {
                        self.refs = response.data;
                    });
                }
            },
            update() {
                //let self = this;
                axios.post('/api/game-tick-scripting', this.config).then(response => {
                    //self.config = response.data;
                });
            }
        }
    }
}

export { createComponent }