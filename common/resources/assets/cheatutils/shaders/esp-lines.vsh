#version 330

layout(std140) uniform Block {
    mat4 MVP;
    vec2 ViewportSize;
};

in vec3 inPointA;
in vec3 inPointB;
in vec4 inColor;
in float inT;      // 0.0 or 1.0
in float inSide;   // -1.0 or +1.0
in float inLineWidth;

noperspective out float vAlongPx;      // 0 .. lineLengthPx
noperspective out float vSidePx;       // -halfWidth .. +halfWidth
noperspective out vec4 vColor;
noperspective out float vLineLengthPx;
noperspective out float vHalfWidthPx;

bool clipPlane(float d0, float d1, inout float t0, inout float t1) {
    float eps = 1e-4;

    if (d0 < eps && d1 < eps) {
        return false;
    }

    if (d0 < eps || d1 < eps) {
        float t = (eps - d0) / (d1 - d0);

        if (d0 < eps) {
            t0 = max(t0, t);
        } else {
            t1 = min(t1, t);
        }

        if (t0 > t1) {
            return false;
        }
    }

    return true;
}

bool clipSegmentToClipBox(inout vec4 a, inout vec4 b) {
    vec4 p0 = a;
    vec4 p1 = b;
    float t0 = 0.0;
    float t1 = 1.0;

    if (!clipPlane(p0.x + p0.w, p1.x + p1.w, t0, t1)) return false; // left
    if (!clipPlane(-p0.x + p0.w, -p1.x + p1.w, t0, t1)) return false; // right
    if (!clipPlane(p0.y + p0.w, p1.y + p1.w, t0, t1)) return false; // bottom
    if (!clipPlane(-p0.y + p0.w, -p1.y + p1.w, t0, t1)) return false; // top
    if (!clipPlane(p0.z + p0.w, p1.z + p1.w, t0, t1)) return false; // near
    if (!clipPlane(-p0.z + p0.w, -p1.z + p1.w, t0, t1)) return false; // far

    a = mix(p0, p1, t0);
    b = mix(p0, p1, t1);
    return true;
}

void main() {
    vec4 clipA = MVP * vec4(inPointA, 1.0);
    vec4 clipB = MVP * vec4(inPointB, 1.0);

    if (!clipSegmentToClipBox(clipA, clipB)) {
        gl_Position = vec4(0.0, 0.0, 0.0, 0.0);
        vAlongPx = 0.0;
        vSidePx = 0.0;
        vColor = vec4(0.0, 0.0, 0.0, 0.0);
        vLineLengthPx = 0.0;
        vHalfWidthPx = 0.0;
        return;
    }

    vec2 ndcA = clipA.xy / clipA.w;
    vec2 ndcB = clipB.xy / clipB.w;

    vec2 screenA = (ndcA * 0.5 + 0.5) * ViewportSize;
    vec2 screenB = (ndcB * 0.5 + 0.5) * ViewportSize;

    vec2 lineVec = screenB - screenA;
    float lineLengthPx = length(lineVec);

    vec2 dir = lineLengthPx > 0.0001 ? (lineVec / lineLengthPx) : vec2(1.0, 0.0);
    vec2 normal = vec2(-dir.y, dir.x);

    float Feather = 1.0;
    float halfWidthPx = inLineWidth * 0.5;
    float halfExtentPx = halfWidthPx + Feather * 0.5;

    vec2 baseScreen = mix(screenA, screenB, inT);
    vec2 expandedScreen = baseScreen + normal * (inSide * halfExtentPx);

    vec4 clip = mix(clipA, clipB, inT);
    vec2 expandedNdc = (expandedScreen / ViewportSize) * 2.0 - 1.0;

    gl_Position = vec4(expandedNdc * clip.w, clip.z, clip.w);

    vAlongPx = inT * lineLengthPx;
    vSidePx = inSide * halfExtentPx;
    vColor = inColor;
    vLineLengthPx = lineLengthPx;
    vHalfWidthPx = halfWidthPx;
}