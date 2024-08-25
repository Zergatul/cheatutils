import { withCss } from '/components/Loader.js';
import { ref, computed, toRefs } from '/vue.esm-browser.js';

let index = 1;

export function createComponent(template) {
    const args = {
        template,
        props: {
            modelValue: {
                type: Boolean,
                required: true
            },
            title: {
                type: String,
                required: false
            }
        },
        setup(props, { emit, slots }) {
            const name = ref('switch' + (index++));
            const { modelValue, title } = toRefs(props);

            const onChange = event => {
                emit('update:modelValue', event.target.checked);
            };

            const hasSlot = computed(() => {
                return !!slots.default && slots.default().length > 0;
            });

            return {
                name,
                hasSlot,
                isChecked: modelValue,
                title,
                onChange
            };
        }
    };

    return withCss(import.meta.url, args);
}