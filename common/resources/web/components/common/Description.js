import * as FallbackLoader from '/fallback-loader.js'
import { withCss } from '/components/Loader.js'

const { ref, computed } = await FallbackLoader.vue();

export function createComponent(template) {
    const args = {
        template,
        setup(props, { slots }) {
            const visible = ref(false);
            const x = ref(0);
            const y = ref(0);

            const hasAdditional = computed(() => {
                return !!slots.additional && slots.additional().length > 0;
            });

            const tooltipStyle = computed(() => {
                return {
                    left: x.value + 'px',
                    top: y.value + 'px'
                };
            });

            const updatePosition = event => {
                if (event.clientX || event.clientY) {
                    x.value = event.clientX + 12;
                    y.value = event.clientY + 12;
                } else {
                    const rect = event.target.getBoundingClientRect();
                    x.value = rect.right + 8;
                    y.value = rect.top;
                }
            };

            const show = event => {
                if (!hasAdditional.value) {
                    return;
                }

                updatePosition(event);
                visible.value = true;
            };

            const hide = () => {
                visible.value = false;
            };

            return {
                visible,
                hasAdditional,
                tooltipStyle,
                show,
                hide,
                updatePosition
            };
        }
    };

    return withCss(import.meta.url, args);
}