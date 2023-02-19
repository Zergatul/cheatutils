function createComponent(template) {
    return {
        template: template,
        created() {
            let self = this;
            axios.get('/api/world-download').then(response => {
                self.setStatus(response);
            });
        },
        data() {
            return {
                loading: false,
                name: '',
                status: ''
            };
        },
        methods: {
            setStatus(response) {
                let self = this;
                if (response.data.active === false) {
                    self.status = 'stopped';
                }
                if (response.data.active === true) {
                    self.status = 'running';
                }
            },
            start() {
                let self = this;
                self.loading = true;
                axios.post('/api/world-download', 'start:' + this.name).then(response => {
                    self.setStatus(response);
                }).catch(error => {
                    alert(error.message + '\n' + error.response.data);
                }).finally(() => {
                    self.loading = false;
                });
            },
            stop() {
                let self = this;
                self.loading = true;
                axios.post('/api/world-download', 'stop').then(response => {
                    self.setStatus(response);
                }).catch(error => {
                    alert(error.message + '\n' + error.response.data);
                }).finally(() => {
                    self.loading = false;
                });;
            }
        }
    }
}

export { createComponent }