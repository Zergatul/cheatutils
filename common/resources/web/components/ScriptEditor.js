let dark = null;
let themeListeners = [];

if (window.matchMedia) {
    dark = window.matchMedia('(prefers-color-scheme: dark)');
    dark.addEventListener('change', () => {
        themeListeners.forEach(callback => callback());
    });
}

function isDark() {
    return dark && dark.matches;
}

function createComponent(template) {
    let eventHandler = null;
    return {
        template: template,
        props: ['modelValue'],
        emits: ['update:modelValue'],
        mounted() {
            let self = this;
            let element = self.$.subTree.el;
            self.editor = ace.edit(element);
            self.editor.setSelectionStyle('text');
            self.editor.setFontSize('16px');
            self.editor.setShowPrintMargin(false);
            self.editor.session.setMode('ace/mode/java');
            if (self.modelValue) {
                self.editor.setValue(self.modelValue, -1);
            }
            self.editor.on('change', () => {
                self.$emit('update:modelValue', self.editor.getValue());
            });

            eventHandler = self.onThemeChanged;
            themeListeners.push(eventHandler);
            eventHandler();
        },
        unmounted() {
            themeListeners = themeListeners.filter(l => l != eventHandler);
        },
        methods: {
            onThemeChanged() {
                const darkTheme = 'ace/theme/one_dark';
                const lightTheme = 'ace/theme/github';
                this.editor.setTheme(isDark() ? darkTheme : lightTheme);
            }
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