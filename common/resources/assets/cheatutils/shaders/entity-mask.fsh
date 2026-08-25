#version 150

uniform sampler2D EntityTexture;

in vec2 texCoords;
out vec4 fragColor;

void main() {
    if (texture(EntityTexture, texCoords).a <= 0.0) {
        discard;
    }
    fragColor = vec4(1.0);
}