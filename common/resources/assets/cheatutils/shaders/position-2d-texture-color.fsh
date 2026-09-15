#version 330
#extension GL_ARB_separate_shader_objects : require

uniform sampler2D Texture0;

layout(location = 0) in vec2 TexCoords;
layout(location = 1) in vec4 Color;

layout(location = 0) out vec4 FragColor;

void main() {
    FragColor = texture(Texture0, TexCoords) * Color;
}