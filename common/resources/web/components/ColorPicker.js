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
            let colorBox = element.querySelector('.color-picker-box');
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
    }
}

export { createComponent }