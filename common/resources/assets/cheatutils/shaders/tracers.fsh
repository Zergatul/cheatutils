#version 330
#extension GL_ARB_separate_shader_objects : require

layout(location = 0) noperspective in float vAlongPx;
layout(location = 1) noperspective in float vSidePx;
layout(location = 2) noperspective in vec4 vColor;
layout(location = 3) noperspective in float vLineLengthPx;
layout(location = 4) noperspective in float vHalfWidthPx;

layout(location = 0) out vec4 fragColor;

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