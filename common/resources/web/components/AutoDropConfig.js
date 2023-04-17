import { addComponent } from '/components/Loader.js'

function createComponent(template) {
    let args = {
        template: template,
        created() {
            let self = this;
            axios.get('/api/auto-drop').then(response => {
                self.config = response.data;
            });
        },
        data() {
            return {
                config: null
            };
        },
        methods: {
            onChange() {
                let self = this;
                axios.post('/api/auto-drop', this.config).then(response => {
                    self.config = response.data;
                });
            }
        }
    };
    addComponent(args, 'ItemsList');
    return args;
}

export { createComponent }