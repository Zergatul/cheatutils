#version 150

uniform sampler2D BufferTexture;
uniform vec4 OutlineColor;
uniform vec2 TexelSize;
in vec2 texCoords;
out vec4 fragColor;

void main() {
    float edge = 8.0 * texture(BufferTexture, texCoords).a;
    for (int y = -1; y <= 1; y++) {
        for (int x = -1; x <= 1; x++) {
            if (x != 0 || y != 0) {
                edge -= texture(BufferTexture, texCoords + vec2(x, y) * TexelSize).a;
            }
        }
    }
    float coverage = clamp(edge * 2.0, 0.0, 1.0);
    fragColor = vec4(OutlineColor.rgb, OutlineColor.a * coverage);
}