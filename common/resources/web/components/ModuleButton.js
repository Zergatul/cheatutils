import * as FallbackLoader from '/fallback-loader.js'

const { computed } = await FallbackLoader.vue();

export function createComponent(template) {
    return {
        template,
        props: {
            module: {
                type: Object,
                required: true
            },
            statuses: {
                type: Object,
                required: true
            },
            filtered: {
                type: Object,
                required: true
            }
        },
        setup(props) {
            const active = computed(() => props.statuses[props.module.statusKey || props.module.component] === true);
            const classes = computed(() => {
                return {
                    'active': active.value,
                    'faded': !props.filtered[props.module.component]
                };
            });

            const label = computed(() => active.value ? `${props.module.name}, enabled` : props.module.name);

            const onClick = () => {
                window.location.hash = '#/' + props.module.path;
            };

            return {
                classes,
                label,
                onClick
            }
        }
    };
};