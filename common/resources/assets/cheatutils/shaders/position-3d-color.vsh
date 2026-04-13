#version 330

layout(std140) uniform Block {
    mat4 MVP;
};

in vec3 InPosition;
in vec4 InColor;

out vec4 Color;

void main() {
    gl_Position = MVP * vec4(InPosition, 1.0);
    Color = InColor;
}