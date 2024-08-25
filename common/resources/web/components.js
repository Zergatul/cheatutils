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
    ColorBox: getComponent('common/ColorBox'),
    ColorPicker: getComponent('common/ColorPicker'),
    ItemList: getComponent('common/ItemList'),
    ScriptEditor: getComponent('common/ScriptEditor'),
    SwitchCheckbox: getComponent('common/SwitchCheckbox')
};

export { components }