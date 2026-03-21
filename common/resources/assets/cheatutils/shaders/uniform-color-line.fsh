#version 330

layout(std140) uniform Block {
    mat4 MVP;
    vec4 Color;
};

out vec4 FragColor;

void main() {
    FragColor = Color;
}