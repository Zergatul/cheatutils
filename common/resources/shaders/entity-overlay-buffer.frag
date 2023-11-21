#version 150

in vec2 TexCoords;

uniform sampler2D EntityTexture;

out vec4 FragColor;

void main() {
    vec4 texColor = texture(EntityTexture, TexCoords);
    FragColor = vec4(1.0, 1.0, 1.0, 1.0) * sign(texColor.a);
}