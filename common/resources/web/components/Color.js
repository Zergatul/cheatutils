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
        let data = this.int32ToRgb(value);
        return '#' + toHex(data[0]) + toHex(data[1]) + toHex(data[2]);
    },
    rgbToInt32(red, green, blue, alpha) {
        return (alpha << 24) | (red << 16) | (green << 8) | (blue);
    }
};

export { Color }