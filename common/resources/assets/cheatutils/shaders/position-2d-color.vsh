#version 330

layout(std140) uniform Inputs {
    mat4 MVP;
};

in vec2 InPosition;
in vec4 InColor;

out vec4 Color;

void main() {
    gl_Position = MVP * vec4(InPosition, 0.0, 1.0);
    Color = InColor;
}