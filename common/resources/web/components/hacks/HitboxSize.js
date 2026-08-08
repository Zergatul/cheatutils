import { createSimpleComponent } from '/components/SimpleModule.js'

export function createComponent(template) {
    return createSimpleComponent('/api/hitbox-size', template, {
        components: ['Radio']
    });
}