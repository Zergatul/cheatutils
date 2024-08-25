import { createSimpleComponent } from '/components/SimpleModule.js';

export function createComponent(template) {
    return createSimpleComponent('/api/projectile-path', template, {
        css: import.meta.url
    });
};