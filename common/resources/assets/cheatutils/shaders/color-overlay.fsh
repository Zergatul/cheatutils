#version 330

uniform sampler2D InSampler;

layout(std140) uniform Block {
    vec4 OverlayColor;
};

in vec2 TexCoordinates;

out vec4 FragColor;

void main() {
    FragColor = texture(InSampler, TexCoordinates) * OverlayColor;
}