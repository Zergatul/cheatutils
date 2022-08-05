import { addComponent } from '/components/Loader.js'

function createComponent(template) {
    let args = {
        template: template,
        created() {
            let self = this;
            axios.get('/api/beacons').then(function (response) {
                self.config = response.data;
            });
            this.loadBeacons();
        },
        data() {
            return {
                config: null,
                beacon: {},
                list: null
            };
        },
        methods: {
            addBeacon() {
                let self = this;
                axios.post('/api/beacons-list', this.beacon).then(function (response) {
                    self.beacon = {};
                    self.loadBeacons();
                }, function (error) {
                    alert(error.response.data);
                });
            },
            loadBeacons() {
                let self = this;
                axios.get('/api/beacons-list').then(function (response) {
                    self.list = response.data;
                });
            },
            update() {
                let self = this;
                axios.post('/api/beacons', this.config).then(function (response) {
                    self.config = response.data;
                });
            }
        }
    };
    addComponent(args, 'ColorBox');
    addComponent(args, 'ColorPicker');
    return args;
}

export { createComponent }