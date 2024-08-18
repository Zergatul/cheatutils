import { createSimpleComponent } from './SimpleModule.js';

function createComponent(template) {
    return createSimpleComponent('/api/bedrock-breaker', template);
}

export { createComponent }