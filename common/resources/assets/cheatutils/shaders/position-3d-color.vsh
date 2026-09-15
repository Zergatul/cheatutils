#version 330
#extension GL_ARB_separate_shader_objects : require

layout(std140) uniform Inputs {
    mat4 MVP;
};

layout(location = 0) in vec3 InPosition;
layout(location = 1) in vec4 InColor;

layout(location = 0) out vec4 Color;

void main() {
    gl_Position = MVP * vec4(InPosition, 1.0);
    Color = InColor;
}