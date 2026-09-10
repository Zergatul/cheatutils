#version 150

uniform sampler2D BufferTexture;
uniform vec4 OverlayColor;

in vec2 texCoords;
out vec4 fragColor;

void main() {
    fragColor = texture(BufferTexture, texCoords) * OverlayColor;
}