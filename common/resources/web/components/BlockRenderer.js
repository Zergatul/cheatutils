function createComponent(template) {
    return {
        template: template,
        props: ['modelValue'],
        mounted() {
            console.log(this.modelValue);
        },
        watch: {
            modelValue(newValue) {
                
            }
        }
    };
}

export { createComponent }