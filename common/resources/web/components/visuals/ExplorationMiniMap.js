import * as http from '/http.js'
import { components } from '/components.js'

export function createComponent(template) {
    const args = {
        template: template,
        created() {
            http.get('/api/exploration-mini-map').then(response => {
                this.config = response;
            });
        },
        data() {
            return {
                config: null,
                markersJson: ''
            };
        },
        methods: {
            addMarker() {
                http.post('/api/exploration-mini-map-markers', {});
            },
            addMarkers() {
                http.put('/api/exploration-mini-map-markers/import', JSON.parse(this.markersJson));
            },
            clearMarkers() {
                http.delete('/api/exploration-mini-map-markers/all', {});
            },
            update() {
                if (this.config.scanFromY == '') {
                    this.config.scanFromY = null;
                }
                http.post('/api/exploration-mini-map', this.config).then(response => {
                    this.config = response;
                });
            }
        }
    };
    components.add(args, 'SwitchCheckbox');
    return args;
}