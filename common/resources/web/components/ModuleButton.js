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
            const classes = computed(() => {
                return {
                    'local-link': !!props.module.localLink,
                    'external-link': !!props.module.externalLink,
                    'dangerous': !!props.module.dangerous,
                    'active': props.statuses[props.module.statusKey || props.module.component] === true,
                    'faded': !props.filtered[props.module.component]
                };
            });

            const label = computed(() => props.statuses[props.module.statusKey || props.module.component] === true
                ? `${props.module.name}, enabled`
                : props.module.name);

            const onClick = () => {
                const module = props.module;
                if (module.onClick) {
                    module.onClick();
                } else {
                    window.location.hash = '#/' + module.path;
                }
            };

            return {
                classes,
                label,
                onClick
            }
        }
    };
}