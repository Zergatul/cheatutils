import { createSimpleComponent } from '/components/SimpleModule.js';

export function createComponent(template) {
    return createSimpleComponent('/api/auto-craft', template, {
        components: ['ItemList'],
        css: import.meta.url
    });
}