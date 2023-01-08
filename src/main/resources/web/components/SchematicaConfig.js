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
                placing: {}
            };
        },
        methods: {
            onFileSelected() {
                let input = this.$refs.fileInput;
                if (input.files.length == 0) {
                    return;
                }

                let self = this;
                let file = input.files[0];
                let reader = new FileReader();
                reader.onload = event => {
                    let result = event.target.result.split(',', 2)[1];
                    axios.post('/api/schematica-upload', result).then(response => {
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
                };
                reader.readAsDataURL(file);
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