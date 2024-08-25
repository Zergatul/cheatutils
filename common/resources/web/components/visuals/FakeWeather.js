import * as http from '/http.js'
import { components } from '/components.js'

export function createComponent(template) {
    const args = {
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
    };
    components.add(args, 'SwitchCheckbox');
    return args;
}