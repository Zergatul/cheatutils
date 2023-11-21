#version 150

in vec2 TexCoords;

uniform sampler2D BufferTexture;
uniform vec4 OverlayColor;
uniform float PixelWidth;
uniform float PixelHeight;

out vec4 FragColor;

void main() {
    float x1 = TexCoords.x - PixelWidth;
    float x2 = TexCoords.x;
    float x3 = TexCoords.x + PixelWidth;
    float y1 = TexCoords.y - PixelHeight;
    float y2 = TexCoords.y;
    float y3 = TexCoords.y + PixelHeight;
    vec4 sum =
        8 * texture(BufferTexture, vec2(x2, y2))
        - texture(BufferTexture, vec2(x1, y1))
        - texture(BufferTexture, vec2(x1, y2))
        - texture(BufferTexture, vec2(x1, y3))
        - texture(BufferTexture, vec2(x2, y1))
        - texture(BufferTexture, vec2(x2, y3))
        - texture(BufferTexture, vec2(x3, y1))
        - texture(BufferTexture, vec2(x3, y2))
        - texture(BufferTexture, vec2(x3, y3));
    float sf = sum.r + sum.g + sum.b + sum.a;
    FragColor = clamp(vec4(sf / 2.0), 0.0, 1.0) * OverlayColor;
}