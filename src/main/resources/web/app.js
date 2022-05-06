(function () {

    const args = {

        data() {
            return {
                newname: null,
                current: 'main',
                search: '',
                blocksConfigList: null,
                blocksConfigMap: null,
                blocksMap: null,
                blocksList: null,
                blockListFiltered: null,
                currentBlockConfig: null,
                entitiesConfigList: null,
                entitiesConfigMap: null,
                entitiesMap: null,
                entitiesList: null,
                entityListFiltered: null,
                currentEntityConfig: null,
                fullBright: null,
                autoFish: null,
                holdUseKey: null,
                lightLevel: {}
            }
        },

        methods: {
            changeName() {
                let self = this;
                axios.post('/api/username', self.newname).then(function (response) {
                    self.name = response.data;
                    document.title = self.name;
                    self.newname = self.name;
                });
            },
            updateFullBright() {
                let self = this;
                axios.post('/api/full-bright', self.fullBright).then(function (response) {
                    self.fullBright = response.data;
                });
            },
            updateAutoFish() {
                let self = this;
                axios.post('/api/auto-fish', self.autoFish).then(function (response) {
                    self.autoFish = response.data;
                });
            },
            updateHoldUseKey() {
                let self = this;
                axios.post('/api/hold-use-key', self.holdUseKey).then(function (response) {
                    self.holdUseKey = response.data;
                });
            },
            updateLightLevel() {
                let self = this;
                axios.post('/api/light-level', self.lightLevel).then(function (response) {
                    self.lightLevel = response.data;
                });
            },
            openBlocksConfig() {
                var self = this;
                self.current = 'blocks-config';
                axios.get('/api/blocks').then(function (response) {
                    self.blocksConfigList = response.data;
                    self.blocksConfigMap = {};
                    self.blocksConfigList.forEach(c => self.blocksConfigMap[c.blockId] = c);
                    self.$nextTick(function () {
                        onBlocksConfigRendered();
                    });
                });
            },
            hardSwitch() {
                var self = this;
                axios.post('/api/hard-switch', true).then(function (response) {
                    alert('ok');
                });
            },
            updateBlockConfig(config) {
                axios.put('/api/blocks/' + config.blockId, config);
            },
            openAddBlockConfig() {
                var self = this;
                self.current = 'add-block';
                self.search = '';
                self.filterBlockList();
            },
            filterBlockList() {
                var self = this;
                let search = self.search.toLocaleLowerCase();
                self.blockListFiltered = self.blocksList.filter(function (block) {
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
                self.$nextTick(function () {
                    onAddBlockConfigRendered();
                });
            },
            backToMain() {
                var self = this;
                self.current = 'main';
            },
            backToBlockConfig() {
                var self = this;
                self.current = 'blocks-config';
                self.$nextTick(function () {
                    onBlocksConfigRendered();
                });
            },
            editBlockConfig(id) {
                var self = this;
                self.current = 'edit-block';
                if (self.blocksConfigMap[id]) {
                    self.currentBlockConfig = self.blocksConfigMap[id];
                } else {
                    self.currentBlockConfig = {
                        blockId: id
                    };
                    axios.post('/api/blocks', self.currentBlockConfig).then(function (response) {
                        self.currentBlockConfig = response.data;
                        self.blocksConfigList.push(self.currentBlockConfig);
                        self.blocksConfigMap[id] = self.currentBlockConfig;
                    });
                }
            },
            deleteBlockConfig() {
                var self = this;
                if (self.currentBlockConfig) {
                    axios.delete('/api/blocks/' + self.currentBlockConfig.blockId).then(function (response) {
                        let id = self.currentBlockConfig.blockId;
                        let index = self.blocksConfigList.indexOf(self.currentBlockConfig);
                        if (index >= 0) {
                            self.blocksConfigList.splice(index, 1);
                        }
                        self.currentBlockConfig = null;
                        delete self.blocksConfigMap[id];
                        self.backToBlockConfig();
                    });
                }
            },
            openEntitiesConfig() {
                var self = this;
                self.current = 'entities-config';
                axios.get('/api/entities').then(function (response) {
                    self.entitiesConfigList = response.data;
                    self.entitiesConfigMap = {};
                    self.entitiesConfigList.forEach(c => self.entitiesConfigMap[c.className] = c);
                });
            },
            updateEntityConfig(config) {
                axios.put('/api/entities/' + config.className, config);
            },
            openAddEntityConfig() {
                var self = this;
                self.current = 'add-entity';
                self.search = '';
                self.filterEntityList();
            },
            filterEntityList() {
                var self = this;
                let search = self.search.toLocaleLowerCase();
                self.entityListFiltered = self.entitiesList.filter(function (entity) {
                    if (entity.simpleName) {
                        let name = entity.simpleName.toLocaleLowerCase();
                        if (name.indexOf(search) >= 0) {
                            return true;
                        }
                    }
                    return false;
                });
            },
            backToEntityConfig() {
                var self = this;
                self.current = 'entities-config';
            },
            editEntityConfig(className) {
                var self = this;
                self.current = 'edit-entity';
                if (self.entitiesConfigMap[className]) {
                    self.currentEntityConfig = self.entitiesConfigMap[className];
                } else {
                    self.currentEntityConfig = {
                        className: className
                    };
                    axios.post('/api/entities', {
                        className: className
                    }).then(function (response) {
                        self.currentEntityConfig = response.data;
                        self.entitiesConfigList.push(self.currentEntityConfig);
                        self.entitiesConfigMap[className] = self.currentEntityConfig;
                    });
                }
            },
            deleteEntityConfig() {
                var self = this;
                if (self.currentEntityConfig) {
                    axios.delete('/api/entities/' + self.currentEntityConfig.className).then(function (response) {
                        let className = self.currentEntityConfig.className;
                        let index = self.entitiesConfigList.indexOf(self.currentEntityConfig);
                        if (index >= 0) {
                            self.entitiesConfigList.splice(index, 1);
                        }
                        self.currentEntityConfig = null;
                        delete self.entitiesConfigMap[className];
                        self.backToEntityConfig();
                    });
                }
            },
            openKillAuraConfig() {
                var self = this;
                axios.get('/api/kill-aura').then(function (response) {
                    self.killAuraConfig = response.data;
                    self.current = 'kill-aura-config';
                });
            },
            updateKillAura() {
                axios.post('/api/kill-aura', this.killAuraConfig);
            }
        },

        created() {
            var self = this;
            axios.get('/api/block-info').then(function (response) {
                self.blocksList = response.data;
                self.blocksMap = {};
                self.blocksList.forEach(b => self.blocksMap[b.id] = b);
            });
            axios.get('/api/entity-info').then(function (response) {
                self.entitiesList = response.data;
                self.entitiesMap = {};
                self.entitiesList.forEach(e => self.entitiesMap[e.className] = e);
            });
            axios.get('/api/username').then(function (response) {
                self.name = response.data;
                document.title = self.name;
                self.newname = self.name;
            });
            axios.get('/api/full-bright').then(function (response) {
                self.fullBright = response.data;
            });
            axios.get('/api/auto-fish').then(function (response) {
                self.autoFish = response.data;
            });
            axios.get('/api/light-level').then(function (response) {
                self.lightLevel = response.data;
            });
            axios.get('/api/hold-use-key').then(function (response) {
                self.holdUseKey = response.data;
            });
        },

        watch: {

        }
    };

    let app = Vue.createApp(args);

    let Color = (function () {
        let toHex = function (value) {
            let result = value.toString(16);
            if (result.length < 2) {
                return '0' + result;
            } else {
                return result;
            }
        };
        return {
            int32ToRgb(value) {
                let blue = value & 0xFF;
                value >>= 8;
                let green = value & 0xFF;
                value >>= 8;
                let red = value & 0xFF;
                value >>= 8;
                let alpha = value & 0xFF;
                return [red, green, blue, alpha];
            },
            int32ToHex(value) {
                let data = this.int32ToRgb(value);
                return '#' + toHex(data[0]) + toHex(data[1]) + toHex(data[2]);
            },
            rgbToInt32(red, green, blue, alpha) {
                return (alpha << 24) | (red << 16) | (green << 8) | (blue);
            },
        };
    })();

    app.component('color-picker', {
        props: ['modelValue'],
        emits: ['update:modelValue'],
        data() {
            return {
                color: -1
            };
        },
        template:
            '<div class="color-picker">' +
            '    <canvas></canvas>' +
            '    <div class="c2">' +
            '        <div class="color-box"></div>' +
            '        <div class="slidercontainer">' +
            '            <input type="range" min="0" max="100" value="50" class="slider">' + 
            '        </div>' +
            '        <div>' +
            '            <input type="text">' +
            '        </div>' +
            '    </div>' +
            '</div>',
        mounted() {
            let self = this;
            self.color = self.modelValue;
            let originalColor = self.color;
            const size = 150;

            let drawColorWheel = function (canvas) {
                canvas.width = size;
                canvas.height = size;
                var context = canvas.getContext("2d");
                var x = size / 2;
                var y = size / 2;
                var radius = size / 2;
                var counterClockwise = false;

                for (var angle = 0; angle <= 360; angle += 1) {
                    var startAngle = (angle-2)*Math.PI/180;
                    var endAngle = angle * Math.PI/180;
                    context.beginPath();
                    context.moveTo(x, y);
                    context.arc(x, y, radius, startAngle, endAngle, counterClockwise);
                    context.closePath();
                    var gradient = context.createRadialGradient(x, y, 0, x, y, radius);
                    gradient.addColorStop(0, 'hsl(' + angle + ', 5%, 100%)');
                    gradient.addColorStop(1, 'hsl(' + angle + ', 100%, 50%)');
                    context.fillStyle = gradient;
                    context.fill();
                }
            };

            let element = self.$.subTree.el;
            let canvas = element.querySelector('canvas');
            let colorBox = element.querySelector('.color-box');
            let slider = element.querySelector('input[type=range]');
            let textBox = element.querySelector('input[type=text]');

            drawColorWheel(canvas);

            self.setColor = function() {
                let hex = Color.int32ToHex(self.color);
                colorBox.style.backgroundColor = hex;
                textBox.value = hex;
            };

            canvas.addEventListener('click', function (event) {
                let x = event.offsetX;
                let y = event.offsetY;
                let r = size / 2;
                let dx = x - r;
                let dy = r - y;
                let d2 = dx * dx + dy * dy;
                if (d2 <= r * r) {
                    let context = canvas.getContext('2d');
                    let data = context.getImageData(x, y, 1, 1).data;
                    self.color = Color.rgbToInt32(data[0], data[1], data[2], data[3]);
                    originalColor = self.color;
                    self.setColor();
                    slider.value = 100;
                    self.$emit('update:modelValue', self.color);
                }
            });

            slider.addEventListener('input', function () {
                let data = Color.int32ToRgb(originalColor);
                for (let i = 0; i < 3; i++) {
                    data[i] = Math.round(data[i] * slider.value / 100);
                }
                self.color = Color.rgbToInt32(data[0], data[1], data[2], data[3]);
                self.setColor();
                self.$emit('update:modelValue', self.color);
            });

            textBox.addEventListener('input', function () {
                if (textBox.value.match(/^#[0-9a-fA-F]{6}$/)) {
                    let red = parseInt(textBox.value.substr(1, 2), 16);
                    let green = parseInt(textBox.value.substr(3, 2), 16);
                    let blue = parseInt(textBox.value.substr(5, 2), 16);
                    let color = Color.rgbToInt32(red, green, blue, 255);
                    if (color != self.color) {
                        self.color = color;
                        self.setColor();
                        self.$emit('update:modelValue', self.color);
                    }
                }
            });

            textBox.addEventListener('blur', function () {
                textBox.value = Color.int32ToHex(self.color);
            });

            self.setColor();
        },
        watch: {
            modelValue: function (newValue) {
                if (this.color != newValue) {
                    this.color = newValue;
                    this.setColor();
                }
            }
        }
    });

    app.mount('#vue-app');

    // 3d cubes

    let truncateId = function (id) {
        if (id.startsWith('minecraft:')) {
            id = id.substr(10);
        }
        if (id.startsWith('block/')) {
            id = id.substr(6);
        }
        return id;
    };

    let extend = function (target, source) {
        for (let property in source) {
            let value = source[property];
            if (value instanceof Object && !(value instanceof Array)) {
                let ext = target[property] || {};
                extend(ext, value);
                target[property] = ext;
            } else {
                target[property] = value;
            }
        }
    };

    let models = {};

    let getModel = function (id) {
        if (!models[id]) {
            models[id] = new Promise(function (resolve, reject) {
                let url = `/assets/minecraft/models/block/${id}.json`;
                axios.get(url).then(function (response) {
                    let model = response.data;
                    if (model.parent) {
                        getModel(truncateId(model.parent)).then(function (parent) {
                            extend(model, parent);
                            model.id = id;
                            resolve(model);
                        }, function () {
                            reject();
                        });
                    } else {
                        model.id = id;
                        resolve(model);
                    }
                }, function () {
                    console.log(`server error while getting model for ${id}`);
                    reject();
                });
            });
        }
        return models[id];
    };

    let blockColors = {};

    let getBlockColor = function (id) {
        if (!blockColors[id]) {
            blockColors[id] = axios.get('/api/block-color/minecraft:' + id).then(function (response) {
                return response.data;
            });
        }
        return blockColors[id];
    };

    let getTexture = function (model, name) {
        if (name.startsWith('#')) {
            return getTexture(model, model.textures[name.substr(1)]);
        } else {
            return name;
        }
    };

    let buildModel = function (model) {

        const baseRotation = 'rotateY(-30deg)';

        let div = document.createElement('div');
        div.classList.add('perspective');

        let ul = document.createElement('ul');
        ul.classList.add('cube');

        if (model.elements) {
            let element = model.elements[0]; // what is in other elements???
            for (let property in element.faces) {
                let face = element.faces[property];
                let texture = getTexture(model, face.texture);
                let faceElement = document.createElement('li');
                faceElement.setAttribute('data-dir', face.cullface);
                switch (face.cullface) {
                    case 'down':
                        faceElement.style.transform = `${baseRotation} translateY(50%) rotateX(-90deg)`;
                        if (face.rotation) {
                            faceElement.style.transform += ` rotateY(${face.rotation}deg)`;
                        }
                        break;
                    case 'up':
                        faceElement.style.transform = `${baseRotation} translateY(-50%) rotateX(-90deg)`;
                        break;
                    case 'north':
                        faceElement.style.transform = `${baseRotation} rotateX(90deg) translateY(50%) rotateX(-90deg)`;
                        break;
                    case 'south':
                        faceElement.style.transform = `${baseRotation} rotateY(180deg) rotateX(90deg) translateY(50%) rotateX(-90deg)`;
                        break;
                    case 'east':
                        faceElement.style.transform = `${baseRotation} translateX(-50%) rotateY(-90deg)`;
                        if (face.rotation) {
                            faceElement.style.transform += ` rotateZ(${face.rotation}deg)`;
                        }
                        break;
                    case 'west':
                        faceElement.style.transform = `${baseRotation} translateX(50%) rotateY(90deg)`;
                        if (face.rotation) {
                            faceElement.style.transform += ` rotateZ(${face.rotation}deg)`;
                        }
                        break;
                    default:
                        faceElement = null;
                        break;
                }
                let setupBackgroundImage = function () {
                    if (faceElement != null) {
                        if (texture.startsWith('minecraft:block/')) {
                            let png = 'url(/assets/minecraft/textures/' + texture.substr(10) + '.png)';
                            faceElement.style.backgroundImage = png;
                            ul.appendChild(faceElement);
                        }
                        if (texture.startsWith('block/')) {
                            let png = 'url(/assets/minecraft/textures/' + texture + '.png)';
                            faceElement.style.backgroundImage = png;
                            ul.appendChild(faceElement);
                        }
                    }
                };
                if (faceElement != null && face.tintindex >= 0) {
                    getBlockColor(model.id).then(function (color) {
                        faceElement.style.backgroundColor = Color.int32ToHex(color);
                        setupBackgroundImage();
                        faceElement.style.backgroundBlendMode = 'multiply'; //'luminosity';
                        faceElement.style.mask = faceElement.style.backgroundImage;
                    });
                }
                else {
                    setupBackgroundImage();
                }
            }
        }

        div.appendChild(ul);

        ul.addEventListener('animationstart', function (event) {
            let first = document.querySelector('ul.cube');
            if (first && first != event.target) {
                event.target.getAnimations()[0].startTime = first.getAnimations()[0].startTime;
            }
        });

        return div;
    };

    let createCube = function (id) {
        id = truncateId(id);
        return new Promise(function (resolve, reject) {
            getModel(id).then(function (model) {
                resolve(buildModel(model));
            }, function () {
                reject();
            })
        });
    };

    // observe

    let observer = new IntersectionObserver(function (entries, opts) {
        entries.forEach(function (entry) {
            if (entry.isIntersecting) {
                observer.unobserve(entry.target);
                let id = entry.target.getAttribute('data-id');
                createCube(id).then(function (div) {
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

})();