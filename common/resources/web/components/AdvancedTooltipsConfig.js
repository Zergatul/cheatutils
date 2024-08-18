import { createSimpleComponent } from './SimpleModule.js';

function createComponent(template) {
    return createSimpleComponent('/api/advanced-tooltips', template);
}

export { createComponent }