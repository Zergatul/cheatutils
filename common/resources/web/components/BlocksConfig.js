import { addComponent } from '/components/Loader.js'
import { createBlockModel } from './BlockModel.js'

let blockInfoPromise = axios.get('/api/block-info').then(function (response) {
    let blocksList = response.data;
    let blocksMap = {};
    blocksList.forEach(b => blocksMap[b.id] = b);
    return {
        blocksList: blocksList,
        blocksMap: blocksMap
    }
});

let observer = new IntersectionObserver(function (entries, opts) {
    entries.forEach(function (entry) {
        if (entry.isIntersecting) {
            observer.unobserve(entry.target);
            let id = entry.target.getAttribute('data-id');
            createBlockModel(id).then(function (div) {
                entry.target.querySelector('div.c1').appendChild(div);
            });
        }
    });
  }, {
    root: document.querySelector('ul.blocks-list'),
    threshold: .5
});

function onBlocksConfigRendered() {
    let list = document.querySelector('ul.blocks-list').children;
    for (let i = 0; i < list.length; i++) {
        observer.observe(list[i]);
    }
}

function onAddBlockConfigRendered() {
    let list = document.querySelector('ul.add-block-list').children;
    for (let i = 0; i < list.length; i++) {
        observer.observe(list[i]);
    }
}

function createComponent(template) {
    let args = {
        template: template,
        created() {
            blockInfoPromise.then(info => {
                this.blocksList = info.blocksList;
                this.blocksMap = info.blocksMap;
            });
            axios.get('/api/blocks').then(response => {
                this.blocksConfigList = response.data;

                this.blocksConfigMap = {};
                this.blocksConfigList.forEach(c => {
                    c.blocks.forEach(b => {
                        this.blocksConfigMap[b] = c;
                    });
                });
                this.$nextTick(() => {
                    onBlocksConfigRendered();
                });
            });
        },
        data() {
            return {
                state: 'list',
                search: '',
                blocksList: null,
                blocksMap: null,
                blocksConfigList: null,
                blocksConfigMap: null,
                selectedConfig: null,
                blockListFiltered: null
            };
        },
        methods: {
            editGroup() {
                this.state = 'edit-group';
                this.filterBlockList();
            },
            backToEdit() {
                this.state = 'edit';
            },
            backToList() {
                this.state = 'list';
                this.blocksConfigList.forEach(config => {
                    config.expanded = false;
                })
                this.$nextTick(function () {
                    onBlocksConfigRendered();
                });
            },
            filterBlockList() {
                let search = this.search.toLocaleLowerCase();
                this.blockListFiltered = this.blocksList.filter(function (block) {
                    if (block.name != null) {
                        let name = block.name.toLocaleLowerCase();
                        if (name.indexOf(search) >= 0) {
                            return true;
                        }
                    }
                    if (block.id != null) {
                        let id = block.id.toLocaleLowerCase();
                        if (id.indexOf(search) >= 0) {
                            return true;
                        }
                    }
                    return false;
                });
                this.$nextTick(function () {
                    onAddBlockConfigRendered();
                });
            },
            openAdd() {
                this.state = 'add';
                this.search = '';
                this.filterBlockList();
            },
            openEdit(id) {
                this.state = 'edit';
                if (this.blocksConfigMap[id]) {
                    this.selectedConfig = this.blocksConfigMap[id];
                    this.selectedConfig.expanded = false;
                } else {
                    this.selectedConfig = null;
                    axios.post('/api/blocks-add', id).then(response => {
                        this.selectedConfig = response.data;
                        this.blocksConfigList.push(this.selectedConfig);
                        this.blocksConfigMap[id] = this.selectedConfig;
                    });
                }
            },
            remove() {
                if (this.selectedConfig) {
                    var self = this;
                    axios.delete('/api/blocks/' + encodeURIComponent(this.selectedConfig.block)).then(function (response) {
                        let id = self.selectedConfig.block;
                        let index = self.blocksConfigList.indexOf(self.selectedConfig);
                        if (index >= 0) {
                            self.blocksConfigList.splice(index, 1);
                        }
                        self.selectedConfig = null;
                        delete self.blocksConfigMap[id];
                        self.backToList();
                    });
                }
            },
            removeById(id) {
                let self = this;
                axios.delete('/api/blocks/' + encodeURIComponent(id)).then(function (response) {
                    let index = self.blocksConfigList.findIndex(b => b.block == id);
                    if (index >= 0) {
                        self.blocksConfigList.splice(index, 1);
                    }
                    delete self.blocksConfigMap[id];
                });
            },
            restart() {
                axios.post('/api/block-esp-restart');
            },
            update(config) {
                if (config.tracerMaxDistance == '') {
                    config.tracerMaxDistance = null;
                }
                if (config.outlineMaxDistance == '') {
                    config.outlineMaxDistance = null;
                }
                axios.post('/api/blocks', config);
            },
            groupEditShouldShowCheckbox(block) {
                return this.blocksConfigMap[block.id] == null || this.blocksConfigMap[block.id] == this.selectedConfig;
            },
            groupEditGetCheckboxSelected(block) {
                return this.blocksConfigMap[block.id] == this.selectedConfig;
            },
            groupEditSetCheckboxSelected(block, event) {
                if (event.target.checked) {
                    if (!this.selectedConfig.blocks.some(id => id == block.id)) {
                        this.selectedConfig.blocks.push(block.id);
                        this.blocksConfigMap[block.id] = this.selectedConfig;
                        this.update(this.selectedConfig);
                    }
                } else {
                    this.selectedConfig.blocks = this.selectedConfig.blocks.filter(id => id != block.id);
                    delete this.blocksConfigMap[block.id];
                    this.update(this.selectedConfig);
                }
            },
            groupEditIsCheckboxDisabled(block) {
                return this.selectedConfig.blocks.length == 1 && this.groupEditGetCheckboxSelected(block);
            }
        }
    };
    addComponent(args, 'ColorBox');
    addComponent(args, 'ColorPicker');
    addComponent(args, 'BlockRenderer');
    return args;
}

export { createComponent }