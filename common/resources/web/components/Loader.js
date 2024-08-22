import * as Vue from '/vue.esm-browser.js';
import * as http from '/http.js';

function addComponent(args, name) {
    if (!args.components) {
        args.components = {};
    }
    args.components[name] = Vue.defineAsyncComponent(function () {
        return new Promise(function (resolve, reject) {
            http.getText(`/components/${name}.html`).then(response => {
                import(`/components/${name}.js`).then(function (module) {
                    resolve(module.createComponent(response));
                }, reject);
            }, reject);
        });
    });
};

function getComponent(name) {
    return Vue.defineAsyncComponent(() => {
        return new Promise((resolve, reject) => {
            http.getText(`/components/${name}.html`).then(response => {
                import(`/components/${name}.js`).then(module => {
                    resolve(module.createComponent(response));
                }, reject);
            }, reject);
        });
    });
}

export { addComponent, getComponent }