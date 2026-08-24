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
        vec4 textureColor = texture2D(uTexture, vTexCoord);
        if (textureColor.a == 0.0) {
            discard;
        }
        gl_FragColor = textureColor * vColor;
    }
`;

const INTERNAL_CANVAS_SIZE = 64;

class BlockRenderingCanvas {

    constructor() {
        this.canvases = [];
        this.blocks = new Map();
        this.textures = new Map();
        this.disposed = false;

        this.canvas = document.createElement('canvas');
        this.canvas.width = INTERNAL_CANVAS_SIZE;
        this.canvas.height = INTERNAL_CANVAS_SIZE;

        const gl = this.canvas.getContext('webgl', { antialias: false });
        if (!gl) {
            throw new Error('WebGL is required for block previews.');
        }
        this.gl = gl;
        this.buildProgram();

        gl.enable(gl.DEPTH_TEST);
        gl.enable(gl.CULL_FACE);
        gl.cullFace(gl.BACK);
        gl.enable(gl.BLEND);
        gl.blendFunc(gl.SRC_ALPHA, gl.ONE_MINUS_SRC_ALPHA);

        this.modelViewMatrix = new Float32Array(16);
        this.modelViewProjectionMatrix = new Float32Array(16);
        this.projectionMatrix = new Float32Array(16);
        perspective(this.projectionMatrix, 0.3, 1, 0.1, 20);

        this.drawAll();
    }

    buildProgram() {
        const gl = this.gl;
        const vertexShader = createShader(gl, vertexShaderSource, gl.VERTEX_SHADER);
        const fragmentShader = createShader(gl, fragmentShaderSource, gl.FRAGMENT_SHADER);
        const program = gl.createProgram();
        gl.attachShader(program, vertexShader);
        gl.attachShader(program, fragmentShader);
        gl.linkProgram(program);

        if (!gl.getProgramParameter(program, gl.LINK_STATUS)) {
            throw new Error(`Program linking failed: ${gl.getProgramInfoLog(program)}`);
        }

        this.program = program;
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

    draw(id, context) {
        const gl = this.gl;
        gl.clearColor(0, 0, 0, 0);
        gl.clear(gl.COLOR_BUFFER_BIT | gl.DEPTH_BUFFER_BIT);

        const blockData = this.blocks.get(id);
        if (blockData?.vertexBuffer && blockData.texture) {
            const textureData = this.textures.get(blockData.texture);
            if (textureData?.glTexture) {
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

        context.clearRect(0, 0, INTERNAL_CANVAS_SIZE, INTERNAL_CANVAS_SIZE);
        context.drawImage(this.canvas, 0, 0, INTERNAL_CANVAS_SIZE, INTERNAL_CANVAS_SIZE);
    }

    drawAll() {
        this.setupMatrix();
        for (const item of this.canvases) {
            this.draw(item.id, item.context);
        }
        if (!this.disposed) {
            requestAnimationFrame(() => this.drawAll());
        }
    }

    requestBlock(id) {
        if (!this.blocks.has(id)) {
            const data = new BlockData();
            this.blocks.set(id, data);
            data.init(this, id);
        }
    }

    requestTexture(id) {
        if (this.textures.has(id)) {
            return;
        }

        const data = {};
        this.textures.set(id, data);
        data.image = new Image();
        data.image.src = '/textures/' + id;
        data.image.addEventListener('load', () => {
            const gl = this.gl;
            const glTexture = gl.createTexture();
            gl.bindTexture(gl.TEXTURE_2D, glTexture);
            gl.texImage2D(gl.TEXTURE_2D, 0, gl.RGBA, gl.RGBA, gl.UNSIGNED_BYTE, data.image);
            gl.texParameteri(gl.TEXTURE_2D, gl.TEXTURE_MIN_FILTER, gl.NEAREST);
            gl.texParameteri(gl.TEXTURE_2D, gl.TEXTURE_MAG_FILTER, gl.NEAREST);
            data.glTexture = glTexture;
        });
    }

    setupMatrix() {
        const distance = 5;
        const angle = performance.now() / 1000 % (2 * Math.PI);
        const x = distance * Math.sin(angle) + 0.5;
        const z = distance * Math.cos(angle) + 0.5;
        lookAt(this.modelViewMatrix, [x, distance / 2 + 0.5, z], [0.5, 0.5, 0.5], [0, 1, 0]);
        multiply(this.modelViewProjectionMatrix, this.projectionMatrix, this.modelViewMatrix);
    }

    createCanvas(div, id) {
        if (!div || !id) {
            return;
        }
        this.deleteCanvas(div);

        const canvas = document.createElement('canvas');
        div.appendChild(canvas);
        canvas.style.width = '100%';
        canvas.style.height = '100%';
        canvas.width = INTERNAL_CANVAS_SIZE;
        canvas.height = INTERNAL_CANVAS_SIZE;
        this.canvases.push({
            canvas,
            context: canvas.getContext('2d'),
            id
        });
        this.requestBlock(id);
    }

    deleteCanvas(div) {
        if (!div) {
            return;
        }
        while (div.firstChild) {
            const index = this.canvases.findIndex(item => item.canvas == div.firstChild);
            if (index >= 0) {
                this.canvases.splice(index, 1);
            }
            div.removeChild(div.firstChild);
        }
    }

    dispose() {
        this.disposed = true;
        this.canvases.length = 0;
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
                buffer.push(vertex.x, vertex.y, vertex.z, vertex.u, vertex.v);
                buffer.push(vertex.r / 255, vertex.g / 255, vertex.b / 255, vertex.a / 255);
            };
            const addQuad = quad => {
                if (quad.vertices.length != 4) {
                    return;
                }
                addVertex(quad.vertices[0]);
                addVertex(quad.vertices[1]);
                addVertex(quad.vertices[2]);
                addVertex(quad.vertices[0]);
                addVertex(quad.vertices[2]);
                addVertex(quad.vertices[3]);
            };
            response.forEach(addQuad);

            this.vertices = buffer.length / 9;
            this.vertexBuffer = gl.createBuffer();
            gl.bindBuffer(gl.ARRAY_BUFFER, this.vertexBuffer);
            gl.bufferData(gl.ARRAY_BUFFER, new Float32Array(buffer), gl.STATIC_DRAW);

            const texture = response[0].location;
            if (!response.every(quad => quad.location == texture)) {
                throw new Error('Not all block quads use the same texture.');
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
        const error = gl.getShaderInfoLog(shader);
        gl.deleteShader(shader);
        throw new Error(`Shader compilation failed: ${error}`);
    }
    return shader;
}

function perspective(out, fovy, aspect, near, far) {
    const f = 1 / Math.tan(fovy / 2);
    out.fill(0);
    out[0] = f / aspect;
    out[5] = f;
    out[11] = -1;
    out[10] = (far + near) / (near - far);
    out[14] = 2 * far * near / (near - far);
}

function lookAt(out, eye, center, up) {
    let z0 = eye[0] - center[0];
    let z1 = eye[1] - center[1];
    let z2 = eye[2] - center[2];
    let length = Math.hypot(z0, z1, z2);
    z0 /= length; z1 /= length; z2 /= length;

    let x0 = up[1] * z2 - up[2] * z1;
    let x1 = up[2] * z0 - up[0] * z2;
    let x2 = up[0] * z1 - up[1] * z0;
    length = Math.hypot(x0, x1, x2);
    x0 /= length; x1 /= length; x2 /= length;

    const y0 = z1 * x2 - z2 * x1;
    const y1 = z2 * x0 - z0 * x2;
    const y2 = z0 * x1 - z1 * x0;

    out[0] = x0; out[1] = y0; out[2] = z0; out[3] = 0;
    out[4] = x1; out[5] = y1; out[6] = z1; out[7] = 0;
    out[8] = x2; out[9] = y2; out[10] = z2; out[11] = 0;
    out[12] = -(x0 * eye[0] + x1 * eye[1] + x2 * eye[2]);
    out[13] = -(y0 * eye[0] + y1 * eye[1] + y2 * eye[2]);
    out[14] = -(z0 * eye[0] + z1 * eye[1] + z2 * eye[2]);
    out[15] = 1;
}

function multiply(out, a, b) {
    for (let column = 0; column < 4; column++) {
        const b0 = b[column * 4];
        const b1 = b[column * 4 + 1];
        const b2 = b[column * 4 + 2];
        const b3 = b[column * 4 + 3];
        out[column * 4] = a[0] * b0 + a[4] * b1 + a[8] * b2 + a[12] * b3;
        out[column * 4 + 1] = a[1] * b0 + a[5] * b1 + a[9] * b2 + a[13] * b3;
        out[column * 4 + 2] = a[2] * b0 + a[6] * b1 + a[10] * b2 + a[14] * b3;
        out[column * 4 + 3] = a[3] * b0 + a[7] * b1 + a[11] * b2 + a[15] * b3;
    }
}

export { BlockRenderingCanvas }