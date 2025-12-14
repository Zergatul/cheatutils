import { createSimpleComponent } from '/components/SimpleModule.js'

export function createComponent(template) {
    return createSimpleComponent('/api/air-place', template, {
        components: ['CodeBlock']
    });
}