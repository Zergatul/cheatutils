import * as Vue from '/vue.esm-browser.js';
import * as http from '/http.js';

export function addComponent(args, name) {
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

export function getComponent(path) {
    return Vue.defineAsyncComponent(async () => {
        const html = await http.getText(`/components/${path}.html`);
        const module = await import(`/components/${path}.js`);
        const component = module.createComponent(html);
        if (component instanceof Promise) {
            return await component;
        } else {
            return component;
        }
    });
}

export function withCss(url, args) {
    const linkId = url.match(/(\w+)\.js$/)[1] + '-CSS';
    if (document.getElementById(linkId) != null) {
        return args;
    } else {
        return new Promise((resolve, reject) => {
            const link = document.createElement('link');
            link.href = url.replace(/\.js$/, '.css');
            link.rel = 'stylesheet';
            link.id = linkId;
            link.onload = () => resolve(args);
            link.onerror = reject;
            document.head.appendChild(link);
        });
    }
}