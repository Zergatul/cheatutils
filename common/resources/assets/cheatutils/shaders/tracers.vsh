#version 330

layout(std140) uniform Inputs {
    mat4 MVP;
    vec2 ViewportSize;
};

in vec3 inTarget;
in vec4 inColor;
in float inLineWidth;

noperspective out float vAlongPx;
noperspective out float vSidePx;
noperspective out vec4 vColor;
noperspective out float vLineLengthPx;
noperspective out float vHalfWidthPx;

const float T_VALUES[6] = float[](0.0, 0.0, 1.0, 0.0, 1.0, 1.0);
const float SIDE_VALUES[6] = float[](-1.0, 1.0, -1.0, 1.0, -1.0, 1.0);

// Artificial depth safely inside both OpenGL and Vulkan clip volumes.
// Tracers are rendered without a depth attachment.
const float TRACER_CLIP_DEPTH = 0.9;
const float CLIP_EPSILON = 1e-4;

void main() {
    vec4 projectedTarget = MVP * vec4(inTarget, 1.0);
    bool behind = projectedTarget.w <= CLIP_EPSILON;
    vec2 targetNdc = projectedTarget.xy / max(abs(projectedTarget.w), CLIP_EPSILON);

    float edgeScale = max(abs(targetNdc.x), abs(targetNdc.y));
    if (behind || edgeScale > 1.0) {
        if (edgeScale < CLIP_EPSILON) {
            // A target exactly behind the camera has no screen-space direction.
            targetNdc = vec2(0.0, -1.0);
        } else {
            targetNdc /= edgeScale;
        }
    }

    float inT = T_VALUES[gl_VertexID % 6];
    float inSide = SIDE_VALUES[gl_VertexID % 6];

    vec2 screenA = ViewportSize * 0.5;
    vec2 screenB = (targetNdc * 0.5 + 0.5) * ViewportSize;

    vec2 lineVec = screenB - screenA;
    float lineLengthPx = length(lineVec);
    vec2 dir = lineLengthPx > 0.0001 ? lineVec / lineLengthPx : vec2(1.0, 0.0);
    vec2 normal = vec2(-dir.y, dir.x);

    float feather = 1.0;
    float halfWidthPx = inLineWidth * 0.5;
    float halfExtentPx = halfWidthPx + feather * 0.5;
    float alongPx = mix(-halfExtentPx, lineLengthPx + halfExtentPx, inT);

    vec2 expandedScreen = screenA + dir * alongPx + normal * (inSide * halfExtentPx);
    vec2 expandedNdc = expandedScreen / ViewportSize * 2.0 - 1.0;

    gl_Position = vec4(expandedNdc, TRACER_CLIP_DEPTH, 1.0);

    vAlongPx = alongPx;
    vSidePx = inSide * halfExtentPx;
    vColor = inColor;
    vLineLengthPx = lineLengthPx;
    vHalfWidthPx = halfWidthPx;
}