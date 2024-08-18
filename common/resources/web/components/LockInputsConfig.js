
import { createSimpleComponent } from './SimpleModule.js';

function createComponent(template) {
    return createSimpleComponent('/api/lock-inputs', template);
}

export { createComponent }