import { Color } from '/color.js'
import { withCss } from '/components/Loader.js'

export function createComponent(template) {
    const args = {
        template: template,
        props: ['color'],
        mounted() {
            let element = this.$.subTree.el;
            element.style.backgroundColor = Color.int32ToHex(this.color);
        }
    };
    return withCss(import.meta.url, args);
};