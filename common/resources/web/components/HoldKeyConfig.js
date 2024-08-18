import { createSimpleComponent } from './SimpleModule.js';

function createComponent(template) {
    return createSimpleComponent('/api/hold-key', template);
}

export { createComponent }