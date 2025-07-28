import * as http from '/http.js'
import { components } from '/components.js'
import { withCss } from '/components/Loader.js'

let blockStatesPromise = null;
let blockStatesFormattedPromise = null;

function getBlockStates() {
    if (blockStatesPromise == null) {
        blockStatesPromise = http.get('/api/block-state');
    }
    return blockStatesPromise;
}

function getBlockStatesFormatted() {
    if (blockStatesFormattedPromise == null) {
        blockStatesFormattedPromise = new Promise((resolve, reject) => {
            getBlockStates().then(states => resolve(states.map(formatBlockState).sort())).catch(reject);
        });
    }
    return blockStatesFormattedPromise;
}

function formatBlockState(state) {
    let result = state.block;
    if (state.properties) {
        result += '[';
        let names = Object.getOwnPropertyNames(state.properties);
        names.sort();
        result += names.map(n => `${n}=${state.properties[n]}`).join(',');
        result += ']';
    }
    return result;
}

export function createComponent(template) {
    const args = {
        template: template,
        created() {
            http.get('/api/schematica').then(response => {
                this.config = response;
                this.onConfigLoaded();
            });
            this.reloadSummaries();
        },
        data() {
            return {
                config: null,
                summaries: null,
                schematic: null,
                slots: null,
                blockStatesFormatted: null,
                placing: {
                    transforms: ['']
                },
                allTransforms: [
                    'Flip X',
                    'Flip Y',
                    'Flip Z',
                    'Rotate X -90deg',
                    'Rotate X +90deg',
                    'Rotate X 180deg',
                    'Rotate Y -90deg',
                    'Rotate Y +90deg',
                    'Rotate Y 180deg',
                    'Rotate Z -90deg',
                    'Rotate Z +90deg',
                    'Rotate Z 180deg',
                ],
                format: 'litematic'
            };
        },
        methods: {
            beginEdit(item) {
                getBlockStatesFormatted().then(states => {
                    if (this.blockStatesFormatted == null) {
                        this.blockStatesFormatted = states;
                    }
                    item.editing = true;
                    item.editText = item.stateFormatted;
                });
            },
            getFile() {
                return new Promise((resolve) => {
                    let input = this.$refs.fileInput;
                    if (input.files.length == 0) {
                        resolve(null);
                        return;
                    }

                    let file = input.files[0];
                    let reader = new FileReader();
                    reader.onload = event => resolve({
                        name: file.name,
                        file: event.target.result.split(',', 2)[1]
                    });
                    reader.readAsDataURL(file);
                });
            },
            onConfigLoaded() {
                this.slots = this.config.autoSelectSlots.join(',');
            },
            onFileSelected() {
                this.getFile().then(file => {
                    if (file == null) {
                        this.schematic = null;
                        return;
                    }
                    http.post('/api/schematica-upload', file).then(response => {
                        if (response.error) {
                            alert(response.error);
                            return;
                        }

                        this.schematic = response;
                        this.schematic.paletteMap = [];
                        for (let i = 0; i < this.schematic.palette.length; i++) {
                            if (this.schematic.summary[i] > 0) {
                                this.schematic.paletteMap.push({
                                    id: i,
                                    count: this.schematic.summary[i],
                                    raw: this.schematic.palette[i].raw,
                                    state: this.schematic.palette[i].state,
                                    stateFormatted: formatBlockState(this.schematic.palette[i].state)
                                });
                            }
                        }
                    }).catch(error => {
                        alert(error.message + '\n' + error.response);
                    });
                });
            },
            onSlotsUpdate() {
                let slots = this.slots.trim().split(',').filter(s => s).map(s => parseInt(s));
                if (slots.some(i => isNaN(i))) {
                    this.onConfigLoaded();
                    alert('Invalid format');
                    return;
                }
                if (slots.some(i => i <= 0 || i >= 10)) {
                    this.onConfigLoaded();
                    alert('Slot number out of range. Use 1..9');
                    return;
                }
                this.config.autoSelectSlots = slots;
                this.update();
            },
            async onItemEditApply(item, state) {
                const states = await getBlockStates();
                const formatted = await getBlockStatesFormatted();
                const index = formatted.indexOf(state);
                if (index < 0) {
                    alert('Cannot find matching block state');
                    item.editing = false;
                    return;
                }
                item.state = states[index];
                item.stateFormatted = state;
                item.editing = false;
            },
            onItemEditCancel(item) {
                item.editing = false;
            },
            place() {
                if (this.schematic.paletteMap.some(e => e.editing)) {
                    alert('Finish BlockState editing before placing');
                    return;
                }
                this.getFile().then(file => {
                    file.placing = this.placing;
                    file.palette = this.schematic.paletteMap.map(e => {
                        return {
                            id: e.id,
                            state: e.state
                        };
                    });
                    http.post('/api/schematica-place', file).then(() => this.reloadSummaries());
                });
            },
            reloadSummaries() {
                http.get('/api/schematica-summary').then(response => this.summaries = response);
            },
            removeAll() {
                http.delete('/api/schematica-summary/all').then(() => this.reloadSummaries());
            },
            removeAt(index) {
                http.delete(`/api/schematica-summary/${index}`).then(() => this.reloadSummaries());
            },
            rescan(index) {
                http.post('/api/schematica-summary', {
                    action: 'rescan',
                    index: index
                }).then(() => this.reloadSummaries());
            },
            move(summary, index, axis) {
                let result = prompt(`Enter new ${axis} coordinate:`, summary[axis]);
                if (result == null) {
                    return;
                }
                let value = parseInt(result);
                if (isNaN(value)) {
                    return;
                }
                summary[axis] = value;
                http.post('/api/schematica-summary', {
                    action: 'move',
                    index: index,
                    x: summary.x,
                    y: summary.y,
                    z: summary.z
                }).then(() => this.reloadSummaries());
            },
            onTransformChanged() {
                if (this.placing.transforms[this.placing.transforms.length - 1] != '') {
                    this.placing.transforms.push('');
                }
                for (let i = 0; i < this.placing.transforms.length - 1; i++) {
                    if (this.placing.transforms[i] == '') {
                        this.placing.transforms.splice(i, 1);
                    }
                }
            },
            update() {
                http.post('/api/schematica', this.config).then(response => {
                    this.config = response;
                    this.onConfigLoaded();
                });
            },
            async download() {
                let extension = null;
                switch (this.format) {
                    case 'litematic': extension = '.litematic'; break;
                    case 'schem-v1': extension = '.schem'; break;
                }

                let response = await http.post('/api/schematica-download', {
                    format: this.format,
                    x1: this.config.create.x1,
                    y1: this.config.create.y1,
                    z1: this.config.create.z1,
                    x2: this.config.create.x2,
                    y2: this.config.create.y2,
                    z2: this.config.create.z2
                });
                if (response.error) {
                    alert(response.error);
                } else {
                    let decoded = atob(response.data);
                    let bytes = new Uint8Array(decoded.length);
                    for (let i = 0; i < bytes.length; i++) {
                        bytes[i] = decoded.charCodeAt(i);
                    }
                    let blob = new Blob([bytes], { type: 'application/octet-stream' });
                    let url = URL.createObjectURL(blob);

                    let anchor = document.createElement('a');
                    anchor.href = url;
                    anchor.download = 'cheatutils' + extension;

                    document.body.appendChild(anchor);
                    anchor.click();
                    document.body.removeChild(anchor);
                    URL.revokeObjectURL(url);
                }
            }
        }
    };

    components.add(args, 'Radio');
    components.add(args, 'SwitchCheckbox');
    components.add(args, 'AutoComplete');

    return withCss(import.meta.url, args);
}