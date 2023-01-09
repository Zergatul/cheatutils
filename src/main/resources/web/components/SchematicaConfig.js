function createComponent(template) {
    return {
        template: template,
        created() {
            let self = this;
            axios.get('/api/schematica').then(response => {
                self.config = response.data;
            });
        },
        data() {
            return {
                config: null,
                schematic: null,
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
            getFileContent() {
                let self = this;
                return new Promise((resolve) => {
                    let input = self.$refs.fileInput;
                    if (input.files.length == 0) {
                        resolve(null);
                        return;
                    }
    
                    let file = input.files[0];
                    let reader = new FileReader();
                    reader.onload = event => resolve(event.target.result.split(',', 2)[1]);
                    reader.readAsDataURL(file);
                });
            },
            onFileSelected() {
                let self = this;
                self.getFileContent().then(content => {
                    axios.post('/api/schematica-upload', content).then(response => {
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
                    });
                });
            },
            place() {
                let self = this;
                self.getFileContent().then(content => {
                    axios.post('/api/schematica-place', {
                        file: content,
                        placing: self.placing
                    });
                });
            },
            update() {
                let self = this;
                axios.post('/api/schematica', this.config).then(response => {
                    self.config = response.data;
                });
            }
        }
    }
}

export { createComponent }