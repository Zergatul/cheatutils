import { components } from '/components.js'

export function createComponent(template) {
    const args = {
        template: template,
    };
    components.add(args, 'CodeBlock');
    return args;
}