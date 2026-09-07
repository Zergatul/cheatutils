#version 150

noperspective in float vAlongPx;
noperspective in float vSidePx;
noperspective in vec4 vColor;
noperspective in float vLineLengthPx;
noperspective in float vHalfWidthPx;

out vec4 fragColor;

void main() {
    float aa = 1.0;
    float distanceToLine;
    if (vAlongPx < 0.0) {
        distanceToLine = length(vec2(vAlongPx, vSidePx));
    } else if (vAlongPx > vLineLengthPx) {
        distanceToLine = length(vec2(vAlongPx - vLineLengthPx, vSidePx));
    } else {
        distanceToLine = abs(vSidePx);
    }
    float alpha = clamp((vHalfWidthPx + aa * 0.5 - distanceToLine) / aa, 0.0, 1.0);
    fragColor = vec4(vColor.rgb, vColor.a * alpha);
}