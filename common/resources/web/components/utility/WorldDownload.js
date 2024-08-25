import * as http from '/http.js';

export function createComponent(template) {
    return {
        template: template,
        created() {
            http.get('/api/world-download').then(response => {
                this.setStatus(response);
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
                if (response.active === false) {
                    this.status = 'stopped';
                }
                if (response.active === true) {
                    this.status = 'running';
                }
            },
            start() {
                this.loading = true;
                http.post('/api/world-download', 'start:' + this.name).then(response => {
                    this.setStatus(response);
                }).catch(error => {
                    alert(error.message + '\n' + error.response);
                }).finally(() => {
                    this.loading = false;
                });
            },
            stop() {
                this.loading = true;
                http.post('/api/world-download', 'stop').then(response => {
                    this.setStatus(response);
                }).catch(error => {
                    alert(error.message + '\n' + error.response);
                }).finally(() => {
                    this.loading = false;
                });;
            }
        }
    };
}