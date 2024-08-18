import { addComponent } from '/components/Loader.js'
import * as http from '/http.js';

function createComponent(template) {
    let args = {
        template: template,
        created() {
            this.resetMarker();
            http.get('/api/world-markers').then(response => {
                this.config = response;
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
                this.marker.color = this.marker.color || 0;
                this.config.entries.push(this.marker);
                http.post('/api/world-markers', this.config).then(response => {
                    this.marker = {};
                    this.config = response;
                }, function (error) {
                    alert(error.response);
                });
            },
            fillCoords() {
                http.get('/api/coordinates').then(response => {
                    if (response) {
                        this.marker.x = response.x.toFixed(3);
                        this.marker.y = response.y.toFixed(3);
                        this.marker.z = response.z.toFixed(3);
                    }
                })
            },
            fillDimension() {
                http.get('/api/dimension').then(response => {
                    if (response) {
                        this.marker.dimension = response;
                    }
                });
            },
            remove(index) {
                this.config.entries.splice(index, 1);
                this.update();
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
                http.post('/api/world-markers', this.config).then(response => {
                    this.config = response;
                });
            }
        }
    };
    addComponent(args, 'ColorBox');
    addComponent(args, 'ColorPicker');
    return args;
}

export { createComponent }