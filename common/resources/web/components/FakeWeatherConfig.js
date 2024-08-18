import * as http from '/http.js';

function createComponent(template) {
    return {
        template: template,
        created() {
            http.get('/api/fake-weather').then(response => {
                this.config = response;
            });
        },
        data() {
            return {
                config: null,
                dayTime: 0,
                rainLevel: 0
            };
        },
        methods: {
            setDayTime() {
                http.post('/api/fake-weather-set-time', { value: this.dayTime });
            },
            setRainLevel() {
                http.post('/api/fake-weather-set-rain', { value: this.rainLevel });
            },
            update() {
                http.post('/api/fake-weather', this.config).then(response => {
                    this.config = response;
                });
            }
        }
    }
}

export { createComponent }