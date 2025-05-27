import * as FallbackLoader from '/fallback-loader.js'
import { components } from '/components.js'
import * as http from '/http.js'

const { ref, computed, toRefs } = await FallbackLoader.vue();

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
                    renderer: 'VANILLA',
                    face: 'Consolas',
                    size: 16,
                    antiAliasing: false,
                    letterSpacing: 0
                };
            }

            const fonts = ref(null);
            fontsPromise.then(f => fonts.value = f);

            const fontDescription = computed(() => {
                const params = modelValue.value;
                if (params.renderer == 'VANILLA') {
                    return null;
                }
                if (fonts.value) {
                    const font = fonts.value.filter(f => f.name == params.face)[0] || null;
                    if (font) {
                        return font.description;
                    }
                }
                return null;
            });

            const update = () => {
                emit('update:modelValue', modelValue.value);
            };

            return {
                modelValue,
                fonts,
                fontDescription,
                update
            };
        }
    };
    components.add(args, 'SwitchCheckbox');
    return args;
}