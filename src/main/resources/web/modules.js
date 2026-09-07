import { getComponent } from '/components/Loader.js'

const modules = {
    all: [],
    automation: {},
    esp: {},
    hacks: {},
    visuals: {},
    scripting: {},
    utility: {}
};

const module = params => {
    modules.all.push(params);
    params.componentRef = getComponent(`${params.group}/${params.component}`);
    modules[params.group][params.component] = params;
};

module({
    group: 'utility',
    name: 'Core Config',
    component: 'Core',
    path: 'core',
    tags: ['core', 'config', 'port', 'http', 'server']
});

module({
    group: 'utility',
    name: 'Profiles',
    component: 'Profiles',
    path: 'profiles',
    tags: ['profiles', 'config', 'settings']
});

export { modules }
