#version 150

uniform sampler2D BufferTexture;
uniform vec4 OutlineColor;
uniform vec2 TexelSize;

in vec2 texCoords;
out vec4 fragColor;

void main() {
    float x1 = texCoords.x - TexelSize.x;
    float x2 = texCoords.x;
    float x3 = texCoords.x + TexelSize.x;
    float y1 = texCoords.y - TexelSize.y;
    float y2 = texCoords.y;
    float y3 = texCoords.y + TexelSize.y;
    vec4 sum =
        8.0 * texture(BufferTexture, vec2(x2, y2))
        - texture(BufferTexture, vec2(x1, y1))
        - texture(BufferTexture, vec2(x1, y2))
        - texture(BufferTexture, vec2(x1, y3))
        - texture(BufferTexture, vec2(x2, y1))
        - texture(BufferTexture, vec2(x2, y3))
        - texture(BufferTexture, vec2(x3, y1))
        - texture(BufferTexture, vec2(x3, y2))
        - texture(BufferTexture, vec2(x3, y3));
    float strength = sum.r + sum.g + sum.b + sum.a;
    fragColor = clamp(vec4(strength / 2.0), 0.0, 1.0) * OutlineColor;
}