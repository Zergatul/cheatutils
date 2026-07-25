#version 330
#extension GL_ARB_separate_shader_objects : require

layout(std140) uniform Inputs {
    mat4 MVP;
    vec2 ViewportSize;
};

layout(location = 0) noperspective in float vAlongPx;
layout(location = 1) noperspective in float vSidePx;
layout(location = 2) noperspective in vec4 vColor;
layout(location = 3) noperspective in float vLineLengthPx;
layout(location = 4) noperspective in float vHalfWidthPx;

layout(location = 0) out vec4 fragColor;

void main()
{
    float aa = 1.0; //max(Feather, 1e-4);

    // Side edge AA
    float distFromCenter = abs(vSidePx);
    float sideAlpha = clamp((vHalfWidthPx + aa * 0.5 - distFromCenter) / aa, 0.0, 1.0);

    // Square cap AA
    float distToStart = vAlongPx;
    float distToEnd = vLineLengthPx - vAlongPx;
    float capAlpha = min(
        clamp((distToStart + aa * 0.5) / aa, 0.0, 1.0),
        clamp((distToEnd + aa * 0.5) / aa, 0.0, 1.0)
    );

    float alpha = sideAlpha * capAlpha;

    fragColor = vec4(vColor.rgb, vColor.a * alpha);
}