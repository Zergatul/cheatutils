#version 330

uniform sampler2D Texture0;

layout(std140) uniform Inputs {
    vec4 OverlayColor;
};

in vec2 TexCoordinates;

out vec4 FragColor;

void main() {
    FragColor = texture(Texture0, TexCoordinates) * OverlayColor;
}