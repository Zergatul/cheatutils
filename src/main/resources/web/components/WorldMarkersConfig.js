import { addComponent } from '/components/Loader.js'

function createComponent(template) {
    let args = {
        template: template,
        created() {
            let self = this;
            self.resetMarker();
            axios.get('/api/world-markers').then(response => {
                self.config = response.data;
            });
        },
        data() {
            return {
                config: null,
                marker: {}
            };
        },
        methods: {
            addMarker() {
                let self = this;
                self.marker.color = self.marker.color || 0;
                self.config.entries.push(self.marker);
                axios.post('/api/world-markers', self.config).then(response => {
                    self.marker = {};
                    self.config = response.data;
                }, function (error) {
                    alert(error.response.data);
                });
            },
            fillCoords() {
                let self = this;
                axios.get('/api/coordinates').then(response => {
                    if (response.data) {
                        self.marker.x = response.data.x.toFixed(3);
                        self.marker.y = response.data.y.toFixed(3);
                        self.marker.z = response.data.z.toFixed(3);
                    }
                })
            },
            fillDimension() {
                let self = this;
                axios.get('/api/dimension').then(response => {
                    if (response.data) {
                        self.marker.dimension = response.data;
                    }
                });
            },
            remove(index) {
                let self = this;
                self.config.entries.splice(index, 1);
                self.update();
            },
            resetMarker() {
                this.marker = {
                    x: 0,
                    y: 0,
                    z: 0,
                    minDistance: 0,
                    color: 0,
                    enabled: true
                };
            },
            update() {
                let self = this;
                axios.post('/api/world-markers', self.config).then(response => {
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