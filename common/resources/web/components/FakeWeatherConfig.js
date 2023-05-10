function createComponent(template) {
    return {
        template: template,
        created() {
            let self = this;
            axios.get('/api/fake-weather').then(function (response) {
                self.config = response.data;
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
                axios.post('/api/fake-weather-set-time', { value: this.dayTime });
            },
            setRainLevel() {
                axios.post('/api/fake-weather-set-rain', { value: this.rainLevel });
            },
            update() {
                let self = this;
                axios.post('/api/fake-weather', this.config).then(function (response) {
                    self.config = response.data;
                });
            }
        }
    }
}

export { createComponent }