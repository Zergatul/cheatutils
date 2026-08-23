import { createSimpleComponent } from '/components/SimpleModule.js'

export function createComponent(template) {
    return createSimpleComponent('/api/elytra-hack', template, {
        components: ['Radio']
    });
}