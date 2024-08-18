
import { createSimpleComponent } from './SimpleModule.js';

function createComponent(template) {
    return createSimpleComponent('/api/performance', template);
}

export { createComponent }