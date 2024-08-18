
import { createSimpleComponent } from './SimpleModule.js';

function createComponent(template) {
    return createSimpleComponent('/api/coordinate-leak-protection', template);
}

export { createComponent }