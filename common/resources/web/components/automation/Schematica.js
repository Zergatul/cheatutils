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
            getBlockStates().then(states => resolve(states.map(formatBlockState))).catch(reject);
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
                    flipX: false,
                    flipY: false,
                    flipZ: false,
                    rotateX: 0,
                    rotateY: 0,
                    rotateZ: 0
                }
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
            update() {
                http.post('/api/schematica', this.config).then(response => {
                    this.config = response;
                    this.onConfigLoaded();
                });
            }
        }
    };

    components.add(args, 'Radio');
    components.add(args, 'SwitchCheckbox');
    components.add(args, 'AutoComplete');

    return withCss(import.meta.url, args);
}