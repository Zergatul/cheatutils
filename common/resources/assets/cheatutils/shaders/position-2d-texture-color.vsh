#version 330
#extension GL_ARB_separate_shader_objects : require

layout(std140) uniform Inputs {
    mat4 MVP;
};

layout(location = 0) in vec2 InPosition;
layout(location = 1) in vec2 InTexCoords;
layout(location = 2) in vec4 InColor;

layout(location = 0) out vec2 TexCoords;
layout(location = 1) out vec4 Color;

void main() {
    gl_Position = MVP * vec4(InPosition, 0.0, 1.0);
    TexCoords = InTexCoords;
    Color = InColor;
}