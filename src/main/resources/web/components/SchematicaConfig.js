function createComponent(template) {
    return {
        template: template,
        created() {
            let self = this;
            axios.get('/api/schematica').then(response => {
                self.config = response.data;
                self.onConfigLoaded();
            });
        },
        data() {
            return {
                config: null,
                schematic: null,
                slots: null,
                placing: {
                    rotateX: 0,
                    rotateY: 0,
                    rotateZ: 0
                }
            };
        },
        methods: {
            clear() {
                axios.delete('/api/schematica-place/_');
            },
            getFile() {
                let self = this;
                return new Promise((resolve) => {
                    let input = self.$refs.fileInput;
                    if (input.files.length == 0) {
                        resolve(null);
                        return;
                    }
    
                    let file = input.files[0];
                    let reader = new FileReader();
                    debugger;
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
                let self = this;
                self.getFile().then(file => {
                    axios.post('/api/schematica-upload', file).then(response => {
                        if (response.data.error) {
                            alert(response.data.error);
                            return;
                        }

                        self.schematic = response.data;
                        self.schematic.paletteMap = [];
                        for (let i = 0; i < self.schematic.palette.length; i++) {
                            if (self.schematic.summary[i] > 0) {
                                self.schematic.paletteMap.push({
                                    id: i,
                                    count: self.schematic.summary[i],
                                    block: self.schematic.palette[i]
                                });
                            }
                        }
                    }).catch(error => {
                        alert(error.message + '\n' + error.response.data);
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
            place() {
                let self = this;
                self.getFileContent().then(file => {
                    file.placing = self.placing;
                    axios.post('/api/schematica-place', file);
                });
            },
            update() {
                let self = this;
                axios.post('/api/schematica', this.config).then(response => {
                    self.config = response.data;
                    self.onConfigLoaded();
                });
            }
        }
    }
}

export { createComponent }