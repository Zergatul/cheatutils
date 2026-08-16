async function loadVue() {
    try {
        const vue = await import('/vue.esm-browser.js');
        console.debug('Vue has been loaded from the bundled version.');
        return vue;
    } catch (error) {
        console.error('Failed to load bundled Vue.', error);
        alert('Vue could not be loaded.');
        throw new Error('Vue could not be loaded.');
    }
}

async function loadMonaco() {
    try {
        const monaco = await import('/local/monaco-editor.js');
        console.debug('Monaco has been loaded from the bundled version.');
        return monaco;
    } catch (error) {
        console.error('Failed to load bundled Monaco.', error);
        alert('Monaco could not be loaded.');
        throw new Error('Monaco could not be loaded.');
    }
}

let vuePromise = null;
let monacoPromise = null;

export function vue() {
    if (!vuePromise) {
        vuePromise = loadVue();
    }
    return vuePromise;
}

export function monaco() {
    if (!monacoPromise) {
        monacoPromise = loadMonaco();
    }
    return monacoPromise;
}