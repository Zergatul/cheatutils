#version 330
#extension GL_ARB_separate_shader_objects : require

layout(location = 0) in vec4 Color;

layout(location = 0) out vec4 FragColor;

void main() {
    FragColor = Color;
}