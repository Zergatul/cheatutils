function toHex(value) {
    let result = value.toString(16);
    if (result.length < 2) {
        return '0' + result;
    } else {
        return result;
    }
};

const Color = {
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
        let [r, g, b] = this.int32ToRgb(value);
        return '#' + toHex(r) + toHex(g) + toHex(b);
    },
    int32ToHexA(value) {
        let [r, g, b, a] = this.int32ToRgb(value);
        return '#' + toHex(r) + toHex(g) + toHex(b) + toHex(a);
    },
    rgbToInt32(red, green, blue, alpha) {
        return (alpha << 24) | (red << 16) | (green << 8) | (blue);
    },
    rgbToHsv(red, green, blue) {
        let r = red / 255;
        let g = green / 255;
        let b = blue / 255;
        let cmin = Math.min(r, g, b);
        let cmax = Math.max(r, g, b);
        let v = cmax;
        let s = cmax != 0 ? (cmax - cmin) / cmax : 0;
        let h;
        if (s == 0) {
            h = 0;
        } else {
            if (cmax == r) {
                h = 60 * ((g - b) / (cmax - cmin)) % 360;
            } else if (cmax == g) {
                h = 60 * ((b - r) / (cmax - cmin)) + 120;
            } else if (cmax == b) {
                h = 60 * ((r - g) / (cmax - cmin)) + 240;
            }
        }
        if (h < 0) {
            h += 360;
        }
        return [Math.round(h), Math.round(s * 100), Math.round(v * 100)];
    },
    hsvToRgb(hue, saturation, value) {
        hue %= 360;
        saturation /= 100;
        value /= 100;
        let chroma = value * saturation;
        let hPrime = hue / 60;
        let x = chroma * (1 - Math.abs(hPrime % 2 - 1));
        let r = 0, g = 0, b = 0;

        if (0 <= hPrime && hPrime < 1) {
            [r, g, b] = [chroma, x, 0];
        } else if (1 <= hPrime && hPrime < 2) {
            [r, g, b] = [x, chroma, 0];
        } else if (2 <= hPrime && hPrime < 3) {
            [r, g, b] = [0, chroma, x];
        } else if (3 <= hPrime && hPrime < 4) {
            [r, g, b] = [0, x, chroma];
        } else if (4 <= hPrime && hPrime < 5) {
            [r, g, b] = [x, 0, chroma];
        } else if (5 <= hPrime && hPrime < 6) {
            [r, g, b] = [chroma, 0, x];
        }

        let m = value - chroma;
        [r, g, b] = [r + m, g + m, b + m]
        return [Math.round(r * 255), Math.round(g * 255), Math.round(b * 255)];
    }
};

export { Color }