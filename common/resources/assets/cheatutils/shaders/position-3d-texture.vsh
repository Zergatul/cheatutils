#version 330

layout(std140) uniform Inputs {
    mat4 MVP;
};

in vec3 Position;
in vec2 TexCoordinates;

out vec2 VertexCoordinates;

void main() {
    gl_Position = MVP * vec4(Position, 1.0);
    VertexCoordinates = TexCoordinates;
}