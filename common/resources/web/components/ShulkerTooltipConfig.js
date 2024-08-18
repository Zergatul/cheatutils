
import { createSimpleComponent } from './SimpleModule.js';

function createComponent(template) {
    return createSimpleComponent('/api/shulker-tooltip', template);
}

export { createComponent }