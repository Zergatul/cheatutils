import { ref, nextTick, onMounted, onUnmounted } from '/vue.esm-browser.js'
import { withCss } from '/components/Loader.js'
import { BlockRenderingCanvas } from './BlockRenderer.js'
import * as http from '/http.js';
import { components } from '../../components.js';
import { SearchSession } from '../../common/SearchSession.js'
import { useIncrementalSearch } from '../../common/IncrementalSearch.js'

const blockInfoPromise = http.get('/api/block-info').then(blocksList => {
    const blocksMap = {};
    blocksList.forEach(b => blocksMap[b.id] = b);
    return {
        blocksList: blocksList,
        blocksMap: blocksMap
    }
});

export function createComponent(template) {
    const args = {
        template: template,
        setup() {
            const root = ref(null);
            const state = ref('list');
            const search = ref('');
            const blocksList = ref(null);
            const blocksMap = ref(null);
            const blocksConfigList = ref(null);
            const blocksConfigMap = ref(null);
            const selectedConfig = ref(null);

            const blockListDiv1 = ref(null);
            const blockListDiv2 = ref(null);
            const visibleItems = ref([]);
            const session = new SearchSession(blocksList, (block, query) => {
                if (query == '') {
                    return true;
                }
                if (block.name != null && block.name.toLocaleLowerCase().indexOf(query) >= 0) {
                    return true;
                }
                if (block.id != null && block.id.toLocaleLowerCase().indexOf(query) >= 0) {
                    return true;
                }
                return false;
            });

            const restartAddNewSearch = useIncrementalSearch({
                queryRef: search,
                itemsRef: visibleItems,
                scrollRootRef: blockListDiv1,
                session,
                onNewItemsAdded: () => nextTick(() => setupObserver()),
                batchSize: 20
            });

            const restartEditGroupSearch = useIncrementalSearch({
                queryRef: search,
                itemsRef: visibleItems,
                scrollRootRef: blockListDiv2,
                session,
                onNewItemsAdded: () => nextTick(() => setupObserver()),
                batchSize: 20
            });

            const blockRenderingKey = 'disableBlockRendering';
            const disableBlockRendering = ref(localStorage.getItem(blockRenderingKey) != null);
            const toggleBlockRendering = () => {
                if (disableBlockRendering.value) {
                    localStorage.removeItem(blockRenderingKey);
                } else {
                    localStorage.setItem(blockRenderingKey, '1');
                }
                location.reload();
            };

            const backToList = () => {
                state.value = 'list';
                blocksConfigList.value.forEach(config => {
                    config.expanded = false;
                });

                nextTick(() => setupObserver());
            };

            const backToEdit = () => {
                state.value = 'edit';
            };

            const openAdd = () => {
                state.value = 'add';
                search.value = '';
                restartAddNewSearch();
            };

            const openEdit = id => {
                state.value = 'edit';
                if (blocksConfigMap.value[id]) {
                    selectedConfig.value = blocksConfigMap.value[id];
                    selectedConfig.value.expanded = false;
                } else {
                    selectedConfig.value = null;
                    http.post('/api/blocks-add', id).then(response => {
                        selectedConfig.value = response;
                        blocksConfigList.value.push(selectedConfig.value);
                        blocksConfigMap.value[id] = selectedConfig.value;
                    });
                }
            };

            const editGroup = () => {
                state.value = 'edit-group';
                restartEditGroupSearch();
            };

            const remove = () => {
                if (selectedConfig.value) {
                    http.delete('/api/blocks/' + encodeURIComponent(selectedConfig.value.blocks[0])).then(() => {
                        blocksConfigList.value = blocksConfigList.value.filter(c => c != selectedConfig.value);
                        selectedConfig.value.blocks.forEach(id => {
                            delete blocksConfigMap.value[id];
                        })
                        selectedConfig.value = null;
                        backToList();
                    });
                }
            };

            const removeById = id => {
                http.delete('/api/blocks/' + encodeURIComponent(id)).then(() => {
                    blocksConfigList.value = blocksConfigList.value.filter(b => !b.blocks.includes(id));
                    delete blocksConfigMap.value[id];
                });
            };

            const rescan = () => {
                http.post('/api/rescan-chunks');
            };

            const update = config => {
                if (config.tracerMaxDistance == '') {
                    config.tracerMaxDistance = null;
                }
                if (config.outlineMaxDistance == '') {
                    config.outlineMaxDistance = null;
                }
                http.post('/api/blocks', config);
            };

            const expandGroup = config => {
                config.expanded = !config.expanded;

                // currentScrollable can't be null here
                const oldItems = [...getChildrenToObserve()];
                nextTick(() => {
                    for (let item of getChildrenToObserve()) {
                        if (!oldItems.includes(item)) {
                            observer.observe(item);
                        }
                    }
                });
            };

            const groupEditShouldShowCheckbox = block => {
                return blocksConfigMap.value[block.id] == null || blocksConfigMap.value[block.id] == selectedConfig.value;
            };

            const groupEditGetCheckboxSelected = block => {
                return blocksConfigMap.value[block.id] == selectedConfig.value;
            };

            const groupEditSetCheckboxSelected = (block, event) => {
                if (event.target.checked) {
                    if (!selectedConfig.value.blocks.some(id => id == block.id)) {
                        selectedConfig.value.blocks.push(block.id);
                        blocksConfigMap.value[block.id] = selectedConfig.value;
                        update(selectedConfig.value);
                    }
                } else {
                    selectedConfig.value.blocks = selectedConfig.value.blocks.filter(id => id != block.id);
                    delete blocksConfigMap.value[block.id];
                    update(selectedConfig.value);
                }
            };

            const groupEditIsCheckboxDisabled = block => {
                return selectedConfig.value.blocks.length == 1 && groupEditGetCheckboxSelected(block);
            };

            let observer = null;
            let currentScrollable = null;

            const getChildrenToObserve = () => {
                return currentScrollable.querySelectorAll(':scope > table > tr');
            };

            const removeObserver = () => {
                if (observer != null) {
                    observer.disconnect();
                    observer = null;
                }
            };

            const renderer = new BlockRenderingCanvas();

            const setupObserver = () => {
                removeObserver();

                if (disableBlockRendering.value) {
                    return;
                }

                blockInfoPromise.then(() => {
                    currentScrollable = document.querySelector('div.block-list');
                    if (currentScrollable == null) {
                        console.error('Cannot find list to initialize observer');
                        return;
                    }
    
                    observer = new IntersectionObserver(entries => {
                        entries.forEach(entry => {
                            if (entry.isIntersecting) {
                                const id = entry.target.getAttribute('data-id');
                                renderer.createCanvas(entry.target.querySelector('div.canvas'), id);
                            } else {
                                renderer.deleteCanvas(entry.target.querySelector('div.canvas'));
                            }
                        });
                      }, {
                        root: currentScrollable,
                        threshold: .5
                    });
    
                    for (let item of getChildrenToObserve()) {
                        observer.observe(item);
                    }
                });
            };

            blockInfoPromise.then(info => {
                blocksList.value = info.blocksList;
                blocksMap.value = info.blocksMap;
            });

            http.get('/api/blocks').then(response => {
                blocksConfigList.value = response;

                blocksConfigMap.value = {};
                blocksConfigList.value.forEach(c => {
                    c.blocks.forEach(b => {
                        blocksConfigMap.value[b] = c;
                    });
                });

                nextTick(() => setupObserver());
            });

            let mutationObserver = null;
            onMounted(() => {
                mutationObserver = new MutationObserver(mutations => {
                    mutations.forEach(mutation => {
                        if (mutation.type === 'childList') {
                            for (let node of mutation.removedNodes) {
                                if (node instanceof HTMLElement) {
                                    node.querySelectorAll('div.canvas').forEach(div => renderer.deleteCanvas(div));
                                }
                            }
                        }
                    });
                });
                mutationObserver.observe(root.value, { childList: true, subtree: true });
            });

            onUnmounted(() => {
                removeObserver();
                if (mutationObserver) {
                    mutationObserver.disconnect();
                }
                renderer.dispose();
            });

            return {
                root,

                state,
                search,
                blocksList,
                blocksMap,
                blocksConfigList,
                blocksConfigMap,
                selectedConfig,
                disableBlockRendering,
                blockListDiv1,
                blockListDiv2,
                visibleItems,

                backToList,
                backToEdit,
                openAdd,
                openEdit,
                editGroup,
                remove,
                removeById,
                rescan,
                update,
                expandGroup,
                toggleBlockRendering,
                groupEditShouldShowCheckbox,
                groupEditGetCheckboxSelected,
                groupEditSetCheckboxSelected,
                groupEditIsCheckboxDisabled
            };
        }
    };

    components.add(args, 'ColorBox');
    components.add(args, 'ColorPicker');
    components.add(args, 'SwitchCheckbox');

    return withCss(import.meta.url, args);
};