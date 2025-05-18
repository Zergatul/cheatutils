import * as FallbackLoader from '/fallback-loader.js'
import { components } from '/components.js'
import * as http from '/http.js'

const { ref, toRefs, onMounted, onUnmounted } = await FallbackLoader.vue();

const fontsPromise = http.get('/api/fonts');

export function createComponent(template) {
    const args = {
        template: template,
        props: {
            modelValue: {
                required: true
            }
        },
        setup(props, { emit }) {
            const { modelValue } = toRefs(props);
            if (!modelValue.value) {
                modelValue.value = {
                    face: 'Consolas',
                    size: 16,
                    antiAliasing: false,
                    letterSpacing: 0
                };
            }

            const fonts = ref(null);
            fontsPromise.then(f => fonts.value = f);

            const update = () => {
                emit('update:modelValue', modelValue.value);
            };

            return {
                modelValue,
                fonts,
                update
            };
        }
    };
    components.add(args, 'SwitchCheckbox');
    return args;
}