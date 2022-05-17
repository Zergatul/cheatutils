import { Color } from './Color.js'

function createComponent(template) {
    return {
        template: template,
        props: ['color'],
        mounted() {
            let element = this.$.subTree.el;
            element.style.backgroundColor = Color.int32ToHex(this.color);
        }
    }
}

export { createComponent }