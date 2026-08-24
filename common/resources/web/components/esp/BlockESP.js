import * as FallbackLoader from '/fallback-loader.js'
import { withCss } from '/components/Loader.js'
import { BlockRenderingCanvas } from './BlockRenderer.js'
import * as http from '/http.js'
import { components } from '../../components.js'
import { SearchSession } from '../../common/SearchSession.js'
import { useIncrementalSearch } from '../../common/IncrementalSearch.js'

const { ref, nextTick, onMounted, onUnmounted } = await FallbackLoader.vue();

const blockInfoPromise = http.get('/api/block-info').then(blocksList => {
    const blocksMap = {};
    blocksList.forEach(block => blocksMap[block.id] = block);
    return {
        blocksList,
        blocksMap
    };
});

export function createComponent(template) {
    const args = {
        template,
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
                if (block.name != null && block.name.toLocaleLowerCase().includes(query)) {
                    return true;
                }
                return block.id != null && block.id.toLocaleLowerCase().includes(query);
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
                blocksConfigList.value.forEach(config => config.expanded = false);
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
                        blocksConfigList.value.push(response);
                        blocksConfigMap.value[id] = response;
                    });
                }
            };

            const editGroup = () => {
                state.value = 'edit-group';
                search.value = '';
                restartEditGroupSearch();
            };

            const remove = () => {
                if (selectedConfig.value) {
                    const config = selectedConfig.value;
                    http.delete('/api/blocks/' + encodeURIComponent(config.blocks[0])).then(() => {
                        blocksConfigList.value = blocksConfigList.value.filter(item => item != config);
                        config.blocks.forEach(id => delete blocksConfigMap.value[id]);
                        selectedConfig.value = null;
                        backToList();
                    });
                }
            };

            const removeById = id => {
                const config = blocksConfigMap.value[id];
                http.delete('/api/blocks/' + encodeURIComponent(id)).then(() => {
                    blocksConfigList.value = blocksConfigList.value.filter(item => item != config);
                    config.blocks.forEach(block => delete blocksConfigMap.value[block]);
                });
            };

            const rescan = () => {
                http.post('/api/block-esp-restart');
            };

            const update = config => {
                if (config.tracerMaxDistance == '') {
                    config.tracerMaxDistance = null;
                }
                if (config.boundingBoxMaxDistance == '') {
                    config.boundingBoxMaxDistance = null;
                }
                http.post('/api/blocks', config);
            };

            const expandGroup = config => {
                config.expanded = !config.expanded;
                const oldItems = currentScrollable ? [...getChildrenToObserve()] : [];
                nextTick(() => {
                    if (observer) {
                        for (const item of getChildrenToObserve()) {
                            if (!oldItems.includes(item)) {
                                observer.observe(item);
                            }
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
                    if (!selectedConfig.value.blocks.includes(block.id)) {
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
                return currentScrollable.querySelectorAll(':scope > table > tbody > tr, :scope > table > tr');
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

                currentScrollable = root.value?.querySelector('div.block-list');
                if (currentScrollable == null) {
                    return;
                }

                observer = new IntersectionObserver(entries => {
                    entries.forEach(entry => {
                        const canvas = entry.target.querySelector('div.canvas');
                        if (entry.isIntersecting) {
                            renderer.createCanvas(canvas, entry.target.getAttribute('data-id'));
                        } else {
                            renderer.deleteCanvas(canvas);
                        }
                    });
                }, {
                    root: currentScrollable,
                    threshold: .5
                });

                for (const item of getChildrenToObserve()) {
                    observer.observe(item);
                }
            };

            blockInfoPromise.then(info => {
                blocksList.value = info.blocksList;
                blocksMap.value = info.blocksMap;
            });

            http.get('/api/blocks').then(response => {
                blocksConfigList.value = response;
                blocksConfigMap.value = {};
                blocksConfigList.value.forEach(config => {
                    config.blocks.forEach(block => blocksConfigMap.value[block] = config);
                });
                nextTick(() => setupObserver());
            });

            let mutationObserver = null;
            onMounted(() => {
                mutationObserver = new MutationObserver(mutations => {
                    mutations.forEach(mutation => {
                        if (mutation.type === 'childList') {
                            for (const node of mutation.removedNodes) {
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
                mutationObserver?.disconnect();
                renderer.dispose();
            });

            return {
                root,
                state,
                search,
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
}