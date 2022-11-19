function createComponent(template) {
    return {
        template: template,
        created() {
            this.refresh();
        },
        data() {
            return {
                config: null,
                refs: null
            };
        },
        methods: {
            refresh() {
                let self = this;
                axios.get('/api/status-overlay').then(function (response) {
                    self.config = response.data;
                });
            },
            save() {
                let self = this;
                axios.post('/api/status-overlay-code', this.config.code).then(function (response) {
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
                    axios.get('/api/scripts-doc/overlay').then(function (response) {
                        self.refs = response.data;
                    });
                }
            },
            update() {
                let self = this;
                axios.post('/api/status-overlay', this.config).then(function (response) {
                    self.config = response.data;
                });
            }
        }
    }
}

export { createComponent }