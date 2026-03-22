import { createSimpleComponent } from '/components/SimpleModule.js';

export function createComponent(template) {
    return createSimpleComponent('/api/elytra-bounce', template, {
        components: ['CodeBlock']
    });
}