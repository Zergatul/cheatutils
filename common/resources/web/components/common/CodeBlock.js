import * as FallbackLoader from '/fallback-loader.js'

const { computed } = await FallbackLoader.vue();

export function createComponent(template) {
    return {
        template,
        props: {
            code: {
                type: String,
                required: true
            }
        },
        setup(props) {
            const stripIndent = text => {
                const lines = text.split('\n');
                while (lines.length && lines[0].trim() == '') {
                    lines.shift();
                }
                while (lines.length && lines[lines.length - 1].trim() == '') {
                    lines.pop();
                }
                const indents = lines
                    .filter(line => line.trim())
                    .map(line => {
                        let leading = 0;
                        while (leading < line.length && line[leading] == ' ') {
                            leading++;
                        }
                        return leading;
                    });
                const minIndent = indents.length ? Math.min(...indents) : 0
                return lines.map(l => l.slice(minIndent)).join('\n');
            };

            const formatted = computed(() => stripIndent(props.code));

            return {
                formatted
            };
        }
    };
};