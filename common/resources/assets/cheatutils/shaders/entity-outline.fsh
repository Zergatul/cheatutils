#version 330

uniform sampler2D Texture0;

layout(std140) uniform Inputs {
    vec4 OutlineColor;
    float PixelWidth;
    float PixelHeight;
};

in vec2 TexCoordinates;

out vec4 FragColor;

void main() {
    float x1 = TexCoordinates.x - PixelWidth;
    float x2 = TexCoordinates.x;
    float x3 = TexCoordinates.x + PixelWidth;
    float y1 = TexCoordinates.y - PixelHeight;
    float y2 = TexCoordinates.y;
    float y3 = TexCoordinates.y + PixelHeight;
    vec4 sum =
        8 * texture(Texture0, vec2(x2, y2))
        - texture(Texture0, vec2(x1, y1))
        - texture(Texture0, vec2(x1, y2))
        - texture(Texture0, vec2(x1, y3))
        - texture(Texture0, vec2(x2, y1))
        - texture(Texture0, vec2(x2, y3))
        - texture(Texture0, vec2(x3, y1))
        - texture(Texture0, vec2(x3, y2))
        - texture(Texture0, vec2(x3, y3));
    float sf = sum.r + sum.g + sum.b + sum.a;
    FragColor = clamp(vec4(sf / 2.0), 0.0, 1.0) * OutlineColor;
}