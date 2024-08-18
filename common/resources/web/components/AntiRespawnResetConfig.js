import { createSimpleComponent } from './SimpleModule.js';

function createComponent(template) {
    return createSimpleComponent('/api/anti-respawn-reset', template);
}

export { createComponent }