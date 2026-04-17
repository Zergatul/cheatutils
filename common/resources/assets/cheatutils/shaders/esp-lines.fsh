#version 330

layout(std140) uniform Inputs {
    mat4 MVP;
    vec2 ViewportSize;
};

noperspective in float vAlongPx;
noperspective in float vSidePx;
noperspective in vec4 vColor;
noperspective in float vLineLengthPx;
noperspective in float vHalfWidthPx;

out vec4 fragColor;

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