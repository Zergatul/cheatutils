import * as FallbackLoader from '/fallback-loader.js'
import { withCss } from '/components/Loader.js'
import { formatCodeResponse } from '/components/MonacoEditor.js'
import * as http from '/http.js'
import { components } from '../../components.js'
import { PrioritySearchSession } from '../../common/PrioritySearchSession.js'
import { useIncrementalSearch } from '../../common/IncrementalSearch.js'

const { ref } = await FallbackLoader.vue();

const entityInfoPromise = http.get('/api/entity-info').then(entitiesList => {
    const entitiesMap = {};
    entitiesList.forEach(e => entitiesMap[e.type] = e);
    return {
        entitiesList: entitiesList,
        entitiesMap: entitiesMap
    }
});

export function createComponent(template) {
    const args = {
        template: template,
        setup() {
            const state = ref('list');
            const search = ref('');
            const entitiesList = ref(null);
            const entitiesMap = ref(null);
            const entitiesConfigList = ref(null);
            const entitiesConfigMap = ref(null);
            const selectedConfig = ref(null);
            const code = ref(null);
            const refs = ref(null);
            const showRefs = ref(false);

            const entityListDiv = ref(null);
            const visibleItems = ref([]);
            const session = new PrioritySearchSession(entitiesList, [
                (entity, query) => query == '',
                (entity, query) => entity.simpleName && entity.simpleName.toLocaleLowerCase().indexOf(query) == 0,
                (entity, query) => entity.simpleName && entity.simpleName.toLocaleLowerCase().indexOf(query) > 0,
                (entity, query) => entity.id && entity.id.indexOf(query) == 0,
                (entity, query) => entity.id && entity.id.indexOf(query) > 0,
                (entity, query) => entity.baseClasses && entity.baseClasses.some(type => type.toLocaleLowerCase().indexOf(query) == 0),
                (entity, query) => entity.baseClasses && entity.baseClasses.some(type => type.toLocaleLowerCase().indexOf(query) > 0),
                (entity, query) => entity.interfaces && entity.interfaces.some(type => type.toLocaleLowerCase().indexOf(query) == 0),
                (entity, query) => entity.interfaces && entity.interfaces.some(type => type.toLocaleLowerCase().indexOf(query) > 0),
            ]);

            const restartSearch = useIncrementalSearch({
                queryRef: search,
                itemsRef: visibleItems,
                scrollRootRef: entityListDiv,
                session,
                batchSize: 20
            });

            const backToList = () => {
                state.value = 'list';
            };

            const moveDown = (config) => {
                http.post('/api/entity-esp-move', {
                    direction: 'down',
                    type: config.type
                }).then(response => {
                    response = response;
                    if (!response.ok) {
                        alert(response.message);
                    } else {
                        loadEntityConfigs();
                    }
                });
            };

            const moveUp = (config) => {
                http.post('/api/entity-esp-move', {
                    direction: 'up',
                    type: config.type
                }).then(response => {
                    if (!response.ok) {
                        alert(response.message);
                    } else {
                        loadEntityConfigs();
                    }
                });
            };

            const openAdd = () => {
                state.value = 'add';
                search.value = '';
                restartSearch();
            };

            const openEdit = (type) => {
                state.value = 'edit';
                const setupCode = () => {
                    code.value = selectedConfig.value.code || '';
                    showRefs.value = false;
                };
                if (entitiesConfigMap.value[type]) {
                    selectedConfig.value = entitiesConfigMap.value[type];
                    setupCode();
                } else {
                    selectedConfig.value = null;
                    http.post('/api/entity-esp', { type: type }).then(response => {
                        selectedConfig.value = response;
                        entitiesConfigList.value.push(response);
                        entitiesConfigMap.value[type] = response;
                        setupCode();
                    });
                }
            };

            const remove = () => {
                if (selectedConfig.value) {
                    http.delete('/api/entity-esp/' + selectedConfig.value.type).then(() => {
                        const type = selectedConfig.value.type;
                        const index = entitiesConfigList.value.indexOf(selectedConfig.value);
                        if (index >= 0) {
                            entitiesConfigList.value.splice(index, 1);
                        }
                        selectedConfig.value = null;
                        delete entitiesConfigMap.value[type];
                        backToList();
                    });
                }
            };

            const removeByClass = (type) => {
                http.delete('/api/entity-esp/' + type).then(() => {
                    const index = entitiesConfigList.value.findIndex(e => e.type == type);
                    if (index >= 0) {
                        entitiesConfigList.value.splice(index, 1);
                    }
                    delete entitiesConfigMap.value[type];
                });
            };

            const update = (config) => {
                if (config.tracerMaxDistance == '') {
                    config.tracerMaxDistance = null;
                }
                if (config.outlineMaxDistance == '') {
                    config.outlineMaxDistance = null;
                }
                if (config.boundingBoxMaxDistance == '') {
                    config.boundingBoxMaxDistance = null;
                }
                http.put('/api/entity-esp/' + config.type, config);
            };

            const showApiRef = () => {
                if (showRefs.value) {
                    showRefs.value = false;
                } else {
                    if (refs.value) {
                        showRefs.value = true;
                    } else {
                        http.get('/api/scripts-doc/ENTITY_ESP').then(response => {
                            showRefs.value = true;
                            refs.value = response;
                        });
                    }
                }
            };

            const saveCode = () => {
                http.post('/api/entity-esp-code', {
                    type: selectedConfig.value.type,
                    code: code.value
                }).then(response => {
                    if (response.ok) {
                        alert('Saved');
                    } else {
                        alert(formatCodeResponse(response));
                    }
                }, error => {
                    alert(error.response);
                });
            };

            entityInfoPromise.then(info => {
                entitiesList.value = info.entitiesList;
                entitiesMap.value = info.entitiesMap;
            });

            const loadEntityConfigs = () => {
                http.get('/api/entity-esp').then(response => {
                    entitiesConfigList.value = response;
                    const map = {};
                    response.forEach(c => map[c.type] = c);
                    entitiesConfigMap.value = map;
                });
            };

            loadEntityConfigs();

            return {
                state,
                search,
                entitiesList,
                entitiesMap,
                entitiesConfigList,
                entitiesConfigMap,
                selectedConfig,
                code,
                refs,
                showRefs,
                entityListDiv,
                visibleItems,
                session,

                backToList,
                moveUp,
                moveDown,
                openAdd,
                openEdit,
                remove,
                removeByClass,
                update,
                showApiRef,
                saveCode,
            };
        }
    };

    components.add(args, 'ColorBox');
    components.add(args, 'ColorPicker');
    components.add(args, 'ScriptEditor');
    components.add(args, 'SwitchCheckbox');

    return withCss(import.meta.url, args);
};