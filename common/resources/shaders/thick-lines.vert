#version 460

layout(std430, binding = 0) buffer TVertex {
    vec3 vertex[];
};

in vec4 in_position;

uniform mat4 u_mvp;
uniform vec2 u_resolution;
uniform float u_thickness;

void main() {
    int lineIndex = gl_VertexID / 6;
    int vertexIndex = gl_VertexID % 6;

    vec4 v1w = u_mvp * vec4(vertex[lineIndex * 6], 1.0);
    vec4 v2w = u_mvp * vec4(vertex[lineIndex * 6 + 3], 1.0);

    vec3 v1 = v1w.xyz / v1w.w;
    vec3 v2 = v2w.xyz / v2w.w;

    vec2 lineVector = (normalize(v2.xy - v1.xy) + 1.0) * 0.5 * u_resolution;
    vec3 orthoVector = vec3(vec2(-lineVector.y, lineVector.x) * u_thickness * 0.5 / u_resolution * 2.0 - 1.0, 0.0);

    vec3 v11 = v1 + orthoVector;
    vec3 v12 = v1 - orthoVector;
    vec3 v21 = v2 + orthoVector;
    vec3 v22 = v2 - orthoVector;

    switch (vertexIndex) {
        case 0: gl_Position = vec4(v11, 1.0); break;
        case 1: gl_Position = vec4(v21, 1.0); break;
        case 2: gl_Position = vec4(v22, 1.0); break;
        case 3: gl_Position = vec4(v11, 1.0); break;
        case 4: gl_Position = vec4(v22, 1.0); break;
        case 5: gl_Position = vec4(v12, 1.0); break;
    }

    gl_Position = u_mvp * vec4(vertex[lineIndex], 1.0);
}