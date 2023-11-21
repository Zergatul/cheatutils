import { Color } from './Color.js'

function createComponent(template) {
    return {
        template: template,
        props: ['modelValue'],
        emits: ['update:modelValue'],
        data() {
            return {
                color: -1
            };
        },
        methods: {
            
        },
        mounted() {
            let self = this;
            self.color = self.modelValue;
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
            let rSlider = element.querySelector('input[type=range].r');
            let gSlider = element.querySelector('input[type=range].g');
            let bSlider = element.querySelector('input[type=range].b');
            let hSlider = element.querySelector('input[type=range].h');
            let sSlider = element.querySelector('input[type=range].s');
            let vSlider = element.querySelector('input[type=range].v');
            let rInput = element.querySelector('input[type=number].r');
            let gInput = element.querySelector('input[type=number].g');
            let bInput = element.querySelector('input[type=number].b');
            let hInput = element.querySelector('input[type=number].h');
            let sInput = element.querySelector('input[type=number].s');
            let vInput = element.querySelector('input[type=number].v');
            var aSlider = element.querySelector('input[type=range].a');
            let aInput = element.querySelector('input[type=number].a');
            let colorBox = element.querySelector('.color-picker-box > div');
            let textBox = element.querySelector('input[type=text]');

            let refreshInputs = (r, g, b, h, s, v, a) => {
                rSlider.value = rInput.value = r;
                gSlider.value = gInput.value = g;
                bSlider.value = bInput.value = b;
                hSlider.value = hInput.value = h;
                sSlider.value = sInput.value = s;
                vSlider.value = vInput.value = v;
                aSlider.value = aInput.value = a;
            };

            let updateR = r => {
                let [, g, b, a] = Color.int32ToRgb(self.color);
                self.setColorRgb(r, g, b, a);
            };
            let updateG = g => {
                let [r, , b, a] = Color.int32ToRgb(self.color);
                self.setColorRgb(r, g, b, a);
            };
            let updateB = b => {
                let [r, g, , a] = Color.int32ToRgb(self.color);
                self.setColorRgb(r, g, b, a);
            };
            let updateA = a => {
                let [r, g, b, ] = Color.int32ToRgb(self.color);
                self.setColorRgb(r, g, b, a);
            }
            let updateH = h => {
                let [, , , a] = Color.int32ToRgb(self.color);
                self.setColorHsv(h, sSlider.value, vSlider.value, a);
            }
            let updateS = s => {
                let [, , , a] = Color.int32ToRgb(self.color);
                self.setColorHsv(hSlider.value, s, vSlider.value, a);
            }
            let updateV = v => {
                let [, , , a] = Color.int32ToRgb(self.color);
                self.setColorHsv(hSlider.value, sSlider.value, v, a);
            }

            rSlider.addEventListener('input', () => updateR(rSlider.value));
            rInput.addEventListener('input', () => updateR(rInput.value));
            gSlider.addEventListener('input', () => updateG(gSlider.value));
            gInput.addEventListener('input', () => updateG(gInput.value));
            bSlider.addEventListener('input', () => updateB(bSlider.value));
            bInput.addEventListener('input', () => updateB(bInput.value));

            hSlider.addEventListener('input', () => updateH(hSlider.value));
            hInput.addEventListener('input', () => updateH(hInput.value));
            sSlider.addEventListener('input', () => updateS(sSlider.value));
            sInput.addEventListener('input', () => updateS(sInput.value));
            vSlider.addEventListener('input', () => updateV(vSlider.value));
            vInput.addEventListener('input', () => updateV(vInput.value));

            aSlider.addEventListener('input', () => updateA(aSlider.value));
            aInput.addEventListener('input', () => updateA(aInput.value));

            drawColorWheel(canvas);

            let updateColorElements = () => {
                colorBox.style.backgroundColor = Color.int32ToHexA(self.color);
                textBox.value = Color.int32ToHex(self.color);
            };

            self.setColorRgb = (r, g, b, a) => {
                self.color = Color.rgbToInt32(r, g, b, a);
                updateColorElements();
                refreshInputs(r, g, b, ...Color.rgbToHsv(r, g, b), a);
                self.$emit('update:modelValue', self.color);
            };

            self.setColorHsv = (h, s, v, a) => {
                let [r, g, b] = Color.hsvToRgb(h, s, v);
                self.color = Color.rgbToInt32(r, g, b, a);
                updateColorElements();
                refreshInputs(r, g, b, h, s, v, a);
                self.$emit('update:modelValue', self.color);
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
                    self.setColorRgb(data[0], data[1], data[2], 255);
                }
            });

            textBox.addEventListener('input', function () {
                if (textBox.value.match(/^#[0-9a-fA-F]{6}$/)) {
                    let red = parseInt(textBox.value.substr(1, 2), 16);
                    let green = parseInt(textBox.value.substr(3, 2), 16);
                    let blue = parseInt(textBox.value.substr(5, 2), 16);
                    let color = Color.rgbToInt32(red, green, blue, 255);
                    if (color != self.color) {
                        self.setColorRgb(red, green, blue, 255);
                    }
                }
            });

            textBox.addEventListener('blur', function () {
                textBox.value = Color.int32ToHex(self.color);
            });

            let rgba = Color.int32ToRgb(self.color);
            self.setColorRgb(rgba[0], rgba[1], rgba[2], rgba[3]);
        },
        watch: {
            modelValue: function (newValue) {
                if (this.color != newValue) {
                    this.color = newValue;
                    this.setColor();
                }
            }
        }
    }
}

export { createComponent }