#version 330

layout(std140) uniform Inputs {
    mat4 MVP;
};

in vec2 InPosition;
in vec2 InTexCoords;
in vec4 InColor;

out vec2 TexCoords;
out vec4 Color;

void main() {
    gl_Position = MVP * vec4(InPosition, 0.0, 1.0);
    TexCoords = InTexCoords;
    Color = InColor;
}