#version 330
#extension GL_ARB_separate_shader_objects : require

layout(std140) uniform Inputs {
    mat4 MVP;
};

layout(location = 0) in vec3 InPosition;
layout(location = 1) in vec2 InTexCoords;

layout(location = 0) out vec2 TextureCoordinates;

void main() {
    gl_Position = MVP * vec4(InPosition, 1.0);
    TextureCoordinates = InTexCoords;
}