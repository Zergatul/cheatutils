import { Color } from './Color.js'

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

let createBlockModel = function (id) {
    id = truncateId(id);
    return new Promise(function (resolve, reject) {
        getModel(id).then(function (model) {
            resolve(buildModel(model));
        }, function () {
            reject();
        })
    });
};

export { createBlockModel }