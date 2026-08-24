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
            let self = this;
            blockInfoPromise.then(function (info) {
                self.blocksList = info.blocksList;
                self.blocksMap = info.blocksMap;
            });
            axios.get('/api/blocks').then(function (response) {
                self.blocksConfigList = response.data;
                self.blocksConfigMap = {};
                self.blocksConfigList.forEach(c => c.blocks.forEach(b => self.blocksConfigMap[b] = c));
                self.$nextTick(function () {
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
                selectedConfig: null
            };
        },
        methods: {
            backToList() {
                this.state = 'list';
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
                } else {
                    this.selectedConfig = null;
                    let self = this;
                    axios.post('/api/blocks-add', id).then(function (response) {
                        self.selectedConfig = response.data;
                        self.blocksConfigList.push(self.selectedConfig);
                        self.blocksConfigMap[id] = self.selectedConfig;
                    });
                }
            },
            remove() {
                if (this.selectedConfig) {
                    var self = this;
                    axios.delete('/api/blocks/' + encodeURIComponent(this.selectedConfig.blocks[0])).then(function (response) {
                        let index = self.blocksConfigList.indexOf(self.selectedConfig);
                        if (index >= 0) {
                            self.blocksConfigList.splice(index, 1);
                        }
                        self.selectedConfig.blocks.forEach(id => delete self.blocksConfigMap[id]);
                        self.selectedConfig = null;
                        self.backToList();
                    });
                }
            },
            removeById(id) {
                let self = this;
                axios.delete('/api/blocks/' + encodeURIComponent(id)).then(function (response) {
                    let config = self.blocksConfigMap[id];
                    let index = self.blocksConfigList.indexOf(config);
                    if (index >= 0) {
                        self.blocksConfigList.splice(index, 1);
                    }
                    if (config) {
                        config.blocks.forEach(block => delete self.blocksConfigMap[block]);
                    }
                });
            },
            restart() {
                axios.post('/api/block-esp-restart');
            },
            update(config) {
                if (config.tracerMaxDistance == '') {
                    config.tracerMaxDistance = null;
                }
                if (config.boundingBoxMaxDistance == '') {
                    config.boundingBoxMaxDistance = null;
                }
                axios.post('/api/blocks', config);
            }
        }
    };
    addComponent(args, 'ColorBox');
    addComponent(args, 'ColorPicker');
    return args;
}

export { createComponent }
