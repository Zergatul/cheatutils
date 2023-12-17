import * as mat4 from '../gl-matrix/mat4.js';

const vertexShaderSource = `
    attribute vec4 aPosition;
    attribute vec2 aTexCoord;
    attribute vec4 aColor;

    uniform mat4 uModelViewProjectionMatrix;

    varying vec2 vTexCoord;
    varying vec4 vColor;

    void main() {
        gl_Position = uModelViewProjectionMatrix * aPosition;
        vTexCoord = aTexCoord;
        vColor = aColor;
    }
`;

const fragmentShaderSource = `
    precision mediump float;

    uniform sampler2D uTexture;

    varying vec2 vTexCoord;
    varying vec4 vColor;

    void main() {
        if (texture2D(uTexture, vTexCoord).a == 0.0) {
            discard;
        }
        gl_FragColor = texture2D(uTexture, vTexCoord) * vColor;
    }
`;

const images = {};

const canvases = [];
const pool = [];

class BlockRendererCanvas {

    static maxCanvases = 20;

    constructor() {
        const canvas = document.createElement('canvas');
        this.canvas = canvas;
        canvas.style.width = '100%';
        canvas.style.height = '100%';

        this.textures = {};

        const gl = this.canvas.getContext('webgl');
        this.gl = gl;
        this.buildProgram();

        gl.enable(gl.DEPTH_TEST);
        gl.enable(gl.CULL_FACE);
        gl.cullFace(gl.BACK);
        gl.enable(gl.BLEND);
        gl.blendFunc(gl.SRC_ALPHA, gl.ONE_MINUS_SRC_ALPHA);
    }

    buildProgram() {
        const gl = this.gl;

        const vertexShader = createShader(gl, vertexShaderSource, gl.VERTEX_SHADER);
        const fragmentShader = createShader(gl, fragmentShaderSource, gl.FRAGMENT_SHADER);

        if (!vertexShader || !fragmentShader) {
            throw 'Cannot compile shader';
        }

        const program = gl.createProgram();
        gl.attachShader(program, vertexShader);
        gl.attachShader(program, fragmentShader);
        gl.linkProgram(program);

        if (!gl.getProgramParameter(program, gl.LINK_STATUS)) {
            console.error('Program linking failed:', gl.getProgramInfoLog(program));
            throw 'Cannot create program';
        }

        this.matrixLocation = gl.getUniformLocation(program, 'uModelViewProjectionMatrix');

        gl.deleteShader(vertexShader);
        gl.deleteShader(fragmentShader);

        gl.useProgram(program);

        this.program = program;
    }

    load(id) {
        const gl = this.gl;
        this.id = id;

        this.disposeBuffer();

        axios.get('/api/block-model/' + encodeURIComponent(id)).then(response => {
            if (this.id != id) {
                return;
            }

            if (response.data.length == 0) {
                this.canDraw = false;
                return;
            }

            let buffer = [];

            const addVertex = vertex => {
                buffer.push(vertex.x);
                buffer.push(vertex.y);
                buffer.push(vertex.z);
                buffer.push(vertex.u);
                buffer.push(vertex.v);
                buffer.push(vertex.r / 255);
                buffer.push(vertex.g / 255);
                buffer.push(vertex.b / 255);
                buffer.push(vertex.a / 255);
            };

            const addQuad = quad => {
                if (quad.vertices.length != 4) {
                    console.error('Invalid quad');
                    return;
                }

                addVertex(quad.vertices[0]);
                addVertex(quad.vertices[1]);
                addVertex(quad.vertices[2]);

                addVertex(quad.vertices[0]);
                addVertex(quad.vertices[2]);
                addVertex(quad.vertices[3]);
            };

            response.data.forEach(quad => addQuad(quad));

            buffer = new Float32Array(buffer);
            this.bufLength = buffer.length / 9;

            this.vertexBuffer = gl.createBuffer();
            gl.bindBuffer(gl.ARRAY_BUFFER, this.vertexBuffer);
            gl.bufferData(gl.ARRAY_BUFFER, buffer, gl.STATIC_DRAW);

            const positionAttributeLocation = gl.getAttribLocation(this.program, 'aPosition');
            gl.enableVertexAttribArray(positionAttributeLocation);
            gl.vertexAttribPointer(positionAttributeLocation, 3, gl.FLOAT, false, 36, 0);

            const texCoordAttributeLocation = gl.getAttribLocation(this.program, 'aTexCoord');
            gl.enableVertexAttribArray(texCoordAttributeLocation);
            gl.vertexAttribPointer(texCoordAttributeLocation, 2, gl.FLOAT, false, 36, 12);

            const colorAttributeLocation = gl.getAttribLocation(this.program, 'aColor');
            gl.enableVertexAttribArray(colorAttributeLocation);
            gl.vertexAttribPointer(colorAttributeLocation, 4, gl.FLOAT, false, 36, 20);

            let textureUrl = response.data[0].location;
            if (!response.data.every(quad => quad.location == textureUrl)) {
                throw 'Not all quads use the same texture';
            }

            if (!this.textures[textureUrl]) {
                loadImage('/textures/' + textureUrl).then(image => {
                    const texture = gl.createTexture();
                    gl.bindTexture(gl.TEXTURE_2D, texture);
    
                    gl.texImage2D(gl.TEXTURE_2D, 0, gl.RGBA, gl.RGBA, gl.UNSIGNED_BYTE, image);
    
                    gl.texParameteri(gl.TEXTURE_2D, gl.TEXTURE_MIN_FILTER, gl.NEAREST);
                    gl.texParameteri(gl.TEXTURE_2D, gl.TEXTURE_MAG_FILTER, gl.NEAREST);

                    this.currentTexture = texture;
                    this.textures[textureUrl] = texture;
    
                    gl.activeTexture(gl.TEXTURE0);
                    gl.bindTexture(gl.TEXTURE_2D, texture);
    
                    const textureUniformLocation = gl.getUniformLocation(this.program, 'uTexture');
                    gl.uniform1i(textureUniformLocation, 0);
    
                    this.canDraw = true;
                });
            } else {
                this.currentTexture = this.textures[textureUrl];

                gl.activeTexture(gl.TEXTURE0);
                gl.bindTexture(gl.TEXTURE_2D, this.currentTexture);

                const textureUniformLocation = gl.getUniformLocation(this.program, 'uTexture');
                gl.uniform1i(textureUniformLocation, 0);

                this.canDraw = true;
            }
        });
    }

    draw(matrix) {
        const gl = this.gl;

        gl.clearColor(0.0, 0.0, 0.0, 0.0);
        gl.clear(gl.COLOR_BUFFER_BIT | gl.DEPTH_BUFFER_BIT);

        if (this.canDraw) {
            gl.uniformMatrix4fv(this.matrixLocation, false, matrix);

            gl.drawArrays(gl.TRIANGLES, 0, this.bufLength);
        }
    }

    dispose() {
        const gl = this.gl;

        this.disposeBuffer();

        gl.bindTexture(gl.TEXTURE_2D, null);
        for (let id in this.textures) {
            gl.deleteTexture(this.textures[id]);
        }

        gl.deleteProgram(this.program);

        this.canDraw = false;
    }

    disposeBuffer() {
        const gl = this.gl;

        if (this.vertexBuffer) {
            gl.bindTexture(gl.TEXTURE_2D, null);
            gl.deleteBuffer(this.vertexBuffer);
            this.bufLength = 0;
        }

        this.canDraw = false;
    }

    static request() {
        if (pool.length > 0) {
            const rc = pool.pop();
            canvases.push(rc);
            return rc;
        }

        if (canvases.length < this.maxCanvases) {
            const rc = new BlockRendererCanvas();
            canvases.push(rc);
            return rc;
        } else {
            return null;
        }
    }
}

function onCanvasRemoved(canvas) {
    for (let i = 0; i < canvases.length; i++) {
        if (canvases[i].canvas == canvas) {
            pool.push(...canvases.splice(i, 1));
            i--;
        }
    }
}

const observer = new MutationObserver((mutations) => {
    mutations.forEach((mutation) => {
        if (mutation.type === "childList") {
            for (let node of mutation.removedNodes) {
                if (node instanceof HTMLElement) {
                    node.querySelectorAll('canvas').forEach(canvas => onCanvasRemoved(canvas));
                    if (node instanceof HTMLCanvasElement) {
                        onCanvasRemoved(node);
                    }
                }
            }
        }
    });
});

observer.observe(document.body, { childList: true, subtree: true });

const modelViewMatrix = mat4.create();
const modelViewProjectionMatrix = mat4.create();
const projectionMatrix = mat4.create();
mat4.perspective(projectionMatrix, 0.3, 1.0, 0.1, 20.0);

function drawAll() {
    if (canvases.length > 0) {
        const time = performance.now();
        const cameraDistance = 5;
        let angle = time / 1000 % (2 * Math.PI);
        let x = cameraDistance * Math.sin(angle) + 0.5;
        let z = cameraDistance * Math.cos(angle) + 0.5;
        mat4.lookAt(modelViewMatrix, [x, cameraDistance / 2 + 0.5, z], [0.5, 0.5, 0.5], [0, 1, 0]);
        mat4.multiply(modelViewProjectionMatrix, projectionMatrix, modelViewMatrix);

        for (let rc of canvases) {
            rc.draw(modelViewProjectionMatrix);
        }
    }

    requestAnimationFrame(() => drawAll());
}

drawAll();

function createShader(gl, sourceCode, type) {
    const shader = gl.createShader(type);
    gl.shaderSource(shader, sourceCode);
    gl.compileShader(shader);

    if (!gl.getShaderParameter(shader, gl.COMPILE_STATUS)) {
        console.error('Shader compilation failed:', gl.getShaderInfoLog(shader));
        gl.deleteShader(shader);
        return null;
    }

    return shader;
}

function loadImage(url) {
    return new Promise((resolve, reject) => {
        if (images[url]) {
            const image = images[url];
            if (image.complete) {
                resolve(image);
            } if (image.failed) {
                reject();
            } else {
                image.addEventListener('load', () => {
                    resolve(image);
                });
                image.addEventListener('error', () => {
                    image.failed = true;
                    reject();
                });
            }
        } else {
            const image = new Image();
            image.src = url;
            images[url] = image;
            loadImage(url).then(resolve).catch(reject);
        }
    });
}

function createBlockRenderer(element, id) {
    removeBlockRenderer(element);

    const rc = BlockRendererCanvas.request();
    if (rc == null) {
        return;
    }

    rc.load(id);
    element.appendChild(rc.canvas);
}

function removeBlockRenderer(element) {
    while (element.firstChild) {
        element.removeChild(element.firstChild);
    }
}

export { createBlockRenderer, removeBlockRenderer, observer }