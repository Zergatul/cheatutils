import { withCss } from '/components/Loader.js'
import { ref, computed, toRefs } from '/vue.esm-browser.js'

let index = 1;

export function createComponent(template) {
    const args = {
        template,
        props: {
            modelValue: {
                required: true
            },
            name: {
                type: String,
                required: true
            },
            value: {
                required: true
            }
        },
        setup(props, { emit }) {
            const id = ref('radio' + (index++));
            const { modelValue, name, value } = toRefs(props);

            const isChecked = computed(() => {
                return modelValue.value == value.value;
            });

            const onChange = event => {
                emit('update:modelValue', event.target.value);
            };

            return {
                id,
                name,
                value,
                isChecked,
                onChange
            };
        }
    };

    return withCss(import.meta.url, args);
}