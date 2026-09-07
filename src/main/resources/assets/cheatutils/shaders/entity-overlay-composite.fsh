#version 150

uniform sampler2D BufferTexture;
uniform vec4 OverlayColor;
in vec2 texCoords;
out vec4 fragColor;

void main() {
    float coverage = texture(BufferTexture, texCoords).a;
    fragColor = vec4(OverlayColor.rgb, OverlayColor.a * coverage);
}