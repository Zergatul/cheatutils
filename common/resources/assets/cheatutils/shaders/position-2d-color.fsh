#version 330

uniform sampler2D Texture;

in vec4 Color;

out vec4 FragColor;

void main() {
    FragColor = Color;
}