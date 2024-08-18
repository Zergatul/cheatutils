
import { createSimpleComponent } from './SimpleModule.js';

function createComponent(template) {
    return createSimpleComponent('/api/end-city-chunks', template);
}

export { createComponent }