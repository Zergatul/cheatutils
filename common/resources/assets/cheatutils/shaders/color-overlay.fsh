#version 330
#extension GL_ARB_separate_shader_objects : require

uniform sampler2D Texture0;

layout(std140) uniform Inputs {
    vec4 OverlayColor;
};

layout(location = 0) in vec2 TexCoordinates;

layout(location = 0) out vec4 FragColor;

void main() {
    FragColor = texture(Texture0, TexCoordinates) * OverlayColor;
}