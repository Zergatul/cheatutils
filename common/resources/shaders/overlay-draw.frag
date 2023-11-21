#version 150

in vec2 TexCoords;

uniform sampler2D BufferTexture;
uniform vec4 OverlayColor;

out vec4 FragColor;

void main() {
    FragColor = texture(BufferTexture, TexCoords) * OverlayColor;
}