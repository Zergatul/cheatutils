import { createSimpleComponent } from './SimpleModule.js';

function createComponent(template) {
    return createSimpleComponent('/api/particles', template);
}

export { createComponent }