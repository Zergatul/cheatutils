import * as mat4 from '/gl-matrix/mat4.js';
import * as http from '/http.js';

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

const INTERNAL_CANVAS_SIZE = 64;

class BlockRenderingCanvas {

    constructor() {
        this.canvases = [];

        this.canvas = document.createElement('canvas');
        this.canvas.style.width = '16px';
        this.canvas.style.height = '16px';
        this.canvas.width = INTERNAL_CANVAS_SIZE;
        this.canvas.height = INTERNAL_CANVAS_SIZE;

        const gl = this.canvas.getContext('webgl');
        this.gl = gl;
        this.buildProgram();

        gl.enable(gl.DEPTH_TEST);
        gl.enable(gl.CULL_FACE);
        gl.cullFace(gl.BACK);
        gl.enable(gl.BLEND);
        gl.blendFunc(gl.SRC_ALPHA, gl.ONE_MINUS_SRC_ALPHA);

        this.modelViewMatrix = mat4.create();
        this.modelViewProjectionMatrix = mat4.create();
        this.projectionMatrix = mat4.create();
        mat4.perspective(this.projectionMatrix, 0.3, 1.0, 0.1, 20.0);

        this.blocks = new Map();
        this.textures = new Map();

        this.drawAll();
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
        this.textureUniformLocation = gl.getUniformLocation(program, 'uTexture');

        gl.deleteShader(vertexShader);
        gl.deleteShader(fragmentShader);

        gl.useProgram(program);

        this.positionAttributeLocation = gl.getAttribLocation(program, 'aPosition');
        gl.enableVertexAttribArray(this.positionAttributeLocation);

        this.texCoordAttributeLocation = gl.getAttribLocation(program, 'aTexCoord');
        gl.enableVertexAttribArray(this.texCoordAttributeLocation);

        this.colorAttributeLocation = gl.getAttribLocation(program, 'aColor');
        gl.enableVertexAttribArray(this.colorAttributeLocation);
    }

    draw(id, ctx2d) {
        const gl = this.gl;

        gl.clearColor(0.0, 0.0, 0.0, 0.0);
        gl.clear(gl.COLOR_BUFFER_BIT | gl.DEPTH_BUFFER_BIT);

        const blockData = this.blocks.get(id);
        if (blockData && blockData.vertexBuffer && blockData.texture) {
            const textureData = this.textures.get(blockData.texture);
            if (textureData && textureData.glTexture) {
                gl.uniformMatrix4fv(this.matrixLocation, false, this.modelViewProjectionMatrix);

                gl.activeTexture(gl.TEXTURE0);
                gl.bindTexture(gl.TEXTURE_2D, textureData.glTexture);
                gl.uniform1i(this.textureUniformLocation, 0);

                gl.bindBuffer(gl.ARRAY_BUFFER, blockData.vertexBuffer);
                gl.vertexAttribPointer(this.positionAttributeLocation, 3, gl.FLOAT, false, 36, 0);
                gl.vertexAttribPointer(this.texCoordAttributeLocation, 2, gl.FLOAT, false, 36, 12);
                gl.vertexAttribPointer(this.colorAttributeLocation, 4, gl.FLOAT, false, 36, 20);

                gl.drawArrays(gl.TRIANGLES, 0, blockData.vertices);
            }
        }

        ctx2d.clearRect(0, 0, INTERNAL_CANVAS_SIZE, INTERNAL_CANVAS_SIZE);
        ctx2d.drawImage(this.canvas, 0, 0, INTERNAL_CANVAS_SIZE, INTERNAL_CANVAS_SIZE);
    }

    drawAll() {
        this.setupMatrix();
        for (let item of this.canvases) {
            this.draw(item.id, item.context);
        }

        if (!this.disposed) {
            requestAnimationFrame(() => this.drawAll());
        }
    }

    requestBlock(id) {
        if (this.blocks.has(id)) {
            return;
        }

        const data = new BlockData();
        this.blocks.set(id, data);
        data.init(this, id);
    }

    requestTexture(id) {
        if (this.textures.has(id)) {
            return;
        }

        const data = {};
        this.textures.set(id, data);

        data.image = new Image();
        data.image.src = '/textures/' + id;

        const processImage = () => {
            const gl = this.gl;
            const glTexture = gl.createTexture();
            gl.bindTexture(gl.TEXTURE_2D, glTexture);

            gl.texImage2D(gl.TEXTURE_2D, 0, gl.RGBA, gl.RGBA, gl.UNSIGNED_BYTE, data.image);

            gl.texParameteri(gl.TEXTURE_2D, gl.TEXTURE_MIN_FILTER, gl.NEAREST);
            gl.texParameteri(gl.TEXTURE_2D, gl.TEXTURE_MAG_FILTER, gl.NEAREST);

            data.glTexture = glTexture;
        };

        if (data.image.complete) {
            processImage();
        } else {
            data.image.addEventListener('load', () => processImage());
        }
    }

    setupMatrix() {
        const time = performance.now();
        const cameraDistance = 5;
        let angle = time / 1000 % (2 * Math.PI);
        let x = cameraDistance * Math.sin(angle) + 0.5;
        let z = cameraDistance * Math.cos(angle) + 0.5;
        mat4.lookAt(this.modelViewMatrix, [x, cameraDistance / 2 + 0.5, z], [0.5, 0.5, 0.5], [0, 1, 0]);
        mat4.multiply(this.modelViewProjectionMatrix, this.projectionMatrix, this.modelViewMatrix);
    }

    createCanvas(div, id) {
        this.deleteCanvas(div);

        const canvas = document.createElement('canvas');
        div.appendChild(canvas);
        canvas.style.width = '100%';
        canvas.style.height = '100%';
        canvas.width = INTERNAL_CANVAS_SIZE;
        canvas.height = INTERNAL_CANVAS_SIZE;
        const context = canvas.getContext('2d');

        this.canvases.push({
            canvas: canvas,
            context: context,
            id: id
        });

        this.requestBlock(id);
    }

    deleteCanvas(div) {
        while (div.firstChild) {
            if (div.firstChild instanceof HTMLCanvasElement) {
                for (let i = 0; i < this.canvases.length; i++) {
                    if (this.canvases[i].canvas == div.firstChild) {
                        this.canvases.splice(i, 1);
                    }
                }
            }
            div.removeChild(div.firstChild);
        }
    }

    dispose() {
        this.disposed = true;
    }
}

class BlockData {

    init(renderer, id) {
        const gl = renderer.gl;
        http.get('/api/block-model/' + encodeURIComponent(id)).then(response => {
            if (response.length == 0) {
                return;
            }

            const buffer = [];

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

            response.forEach(quad => addQuad(quad));

            this.vertices = buffer.length / 9;

            this.vertexBuffer = gl.createBuffer();
            gl.bindBuffer(gl.ARRAY_BUFFER, this.vertexBuffer);
            gl.bufferData(gl.ARRAY_BUFFER, new Float32Array(buffer), gl.STATIC_DRAW);

            let texture = response[0].location;
            if (!response.every(quad => quad.location == texture)) {
                throw 'Not all quads use the same texture';
            }

            renderer.requestTexture(texture);
            this.texture = texture;
        });
    }
}

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

export { BlockRenderingCanvas }