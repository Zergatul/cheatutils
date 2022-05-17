import * as Vue from '/vue.esm-browser.js';

function addComponent(args, name) {
    if (!args.components) {
        args.components = {};
    }
    args.components[name] = Vue.defineAsyncComponent(function () {
        return new Promise(function (resolve, reject) {
            axios.get(`/components/${name}.html`).then(function (response) {
                let template = response.data;
                import(`/components/${name}.js`).then(function (module) {
                    resolve(module.createComponent(template));
                }, reject);
            }, reject);
        });
    });
};

export { addComponent }