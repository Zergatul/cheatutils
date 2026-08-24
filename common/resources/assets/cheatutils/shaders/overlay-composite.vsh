#version 150

out vec2 texCoords;

const vec2 POSITIONS[3] = vec2[3](
    vec2(-1.0, -1.0),
    vec2(3.0, -1.0),
    vec2(-1.0, 3.0)
);

void main() {
    vec2 position = POSITIONS[gl_VertexID];
    gl_Position = vec4(position, 0.0, 1.0);
    texCoords = position * 0.5 + 0.5;
}