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
    ScriptEditor: getComponent('common/ScriptEditor'),
    CodeBlock: getComponent('common/CodeBlock'),
    SwitchCheckbox: getComponent('common/SwitchCheckbox')
};

export { components }