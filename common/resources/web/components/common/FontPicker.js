import * as FallbackLoader from '/fallback-loader.js'
import * as http from '/http.js'

const { ref, toRefs, onMounted, onUnmounted } = await FallbackLoader.vue();

const fontsPromise = http.get('/api/fonts');

export function createComponent(template) {
    return {
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
                    size: 16
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
}