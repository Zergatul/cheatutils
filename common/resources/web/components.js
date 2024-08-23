import { getComponent } from '/components/Loader.js'

const components = {
    add(args, name) {
        if (components[name]) {
            if (!args.components) {
                args.components = {};
            }
            args.components[name] = components[name];
        }
    },
    ItemList: getComponent('common/ItemList'),
    SwitchCheckbox: getComponent('common/SwitchCheckbox')
};

export { components }