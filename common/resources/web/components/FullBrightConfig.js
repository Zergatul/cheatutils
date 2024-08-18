import { createSimpleComponent } from './SimpleModule.js';

function createComponent(template) {
    return createSimpleComponent('/api/full-bright', template);
}

export { createComponent }