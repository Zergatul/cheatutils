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
    Radio: getComponent('common/Radio'),
    ScriptEditor: getComponent('common/ScriptEditor'),
    CodeBlock: getComponent('common/CodeBlock'),
    SwitchCheckbox: getComponent('common/SwitchCheckbox'),
    AutoComplete: getComponent('common/AutoComplete')
};

export { components }