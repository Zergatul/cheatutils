function createComponent(template) {
    return {
        template: template,
        props: ['modelValue'],
        emits: ['update:modelValue'],
        mounted() {
            let self = this;
            let element = self.$.subTree.el;
            self.editor = ace.edit(element);
            self.editor.setTheme("ace/theme/github");
            self.editor.setSelectionStyle('text');
            self.editor.setFontSize('16px');
            self.editor.setShowPrintMargin(false);
            self.editor.session.setMode("ace/mode/java");
            if (self.modelValue) {
                self.editor.setValue(self.modelValue, -1);
            }
            self.editor.on('change', () => {
                self.$emit('update:modelValue', self.editor.getValue());
            });
        },
        watch: {
            modelValue(value) {
                if (this.editor.getValue() != value) {
                    this.editor.setValue(value, -1);
                }
            }
        }
    };
}

export { createComponent }