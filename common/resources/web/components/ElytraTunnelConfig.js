
import { createSimpleComponent } from './SimpleModule.js';

function createComponent(template) {
    return createSimpleComponent('/api/elytra-tunnel', template);
}

export { createComponent }