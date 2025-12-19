import { createSimpleComponent } from '/components/SimpleModule.js'

export function createComponent(template) {
    return createSimpleComponent('/api/breach-swap', template, {
        components: ['CodeBlock']
    });
}