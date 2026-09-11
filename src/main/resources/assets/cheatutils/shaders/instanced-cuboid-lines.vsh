#version 150

uniform mat4 MVP;
uniform vec2 ViewportSize;

in vec3 inOrigin;
in vec3 inSize;
in vec4 inColor;
in float inLineWidth;

noperspective out float vAlongPx;
noperspective out float vSidePx;
noperspective out vec4 vColor;
noperspective out float vLineLengthPx;
noperspective out float vHalfWidthPx;

const float T_VALUES[6] = float[6](0.0, 0.0, 1.0, 0.0, 1.0, 1.0);
const float SIDE_VALUES[6] = float[6](-1.0, 1.0, -1.0, 1.0, -1.0, 1.0);

const vec3 CORNERS[8] = vec3[8](
    vec3(0.0, 0.0, 0.0),
    vec3(1.0, 0.0, 0.0),
    vec3(1.0, 0.0, 1.0),
    vec3(0.0, 0.0, 1.0),
    vec3(0.0, 1.0, 0.0),
    vec3(1.0, 1.0, 0.0),
    vec3(1.0, 1.0, 1.0),
    vec3(0.0, 1.0, 1.0)
);

const int EDGE_START[12] = int[12](0, 3, 2, 1, 4, 7, 6, 5, 0, 3, 2, 1);
const int EDGE_END[12] = int[12](3, 2, 1, 0, 7, 6, 5, 4, 4, 7, 6, 5);

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

    if (!clipPlane(p0.x + p0.w, p1.x + p1.w, t0, t1)) return false;
    if (!clipPlane(-p0.x + p0.w, -p1.x + p1.w, t0, t1)) return false;
    if (!clipPlane(p0.y + p0.w, p1.y + p1.w, t0, t1)) return false;
    if (!clipPlane(-p0.y + p0.w, -p1.y + p1.w, t0, t1)) return false;
    if (!clipPlane(p0.z + p0.w, p1.z + p1.w, t0, t1)) return false;
    if (!clipPlane(-p0.z + p0.w, -p1.z + p1.w, t0, t1)) return false;

    a = mix(p0, p1, t0);
    b = mix(p0, p1, t1);
    return true;
}

void main() {
    int lineIndex = gl_VertexID / 6;
    int vertexIndex = gl_VertexID % 6;

    vec3 pointA = inOrigin + CORNERS[EDGE_START[lineIndex]] * inSize;
    vec3 pointB = inOrigin + CORNERS[EDGE_END[lineIndex]] * inSize;
    vec4 clipA = MVP * vec4(pointA, 1.0);
    vec4 clipB = MVP * vec4(pointB, 1.0);

    if (!clipSegmentToClipBox(clipA, clipB)) {
        gl_Position = vec4(0.0);
        vAlongPx = 0.0;
        vSidePx = 0.0;
        vColor = vec4(0.0);
        vLineLengthPx = 0.0;
        vHalfWidthPx = 0.0;
        return;
    }

    float inT = T_VALUES[vertexIndex];
    float inSide = SIDE_VALUES[vertexIndex];
    vec2 ndcA = clipA.xy / clipA.w;
    vec2 ndcB = clipB.xy / clipB.w;
    vec2 screenA = (ndcA * 0.5 + 0.5) * ViewportSize;
    vec2 screenB = (ndcB * 0.5 + 0.5) * ViewportSize;
    vec2 lineVec = screenB - screenA;
    float lineLengthPx = length(lineVec);
    vec2 dir = lineLengthPx > 0.0001 ? lineVec / lineLengthPx : vec2(1.0, 0.0);
    vec2 normal = vec2(-dir.y, dir.x);

    float halfWidthPx = inLineWidth * 0.5;
    float halfExtentPx = halfWidthPx + 0.5;
    vec2 baseScreen = mix(screenA, screenB, inT);
    vec2 expandedScreen = baseScreen + normal * (inSide * halfExtentPx);
    vec4 clip = mix(clipA, clipB, inT);
    vec2 expandedNdc = expandedScreen / ViewportSize * 2.0 - 1.0;

    gl_Position = vec4(expandedNdc * clip.w, clip.z, clip.w);
    vAlongPx = inT * lineLengthPx;
    vSidePx = inSide * halfExtentPx;
    vColor = inColor;
    vLineLengthPx = lineLengthPx;
    vHalfWidthPx = halfWidthPx;
}