#version 330

uniform sampler2D Texture0;

in vec2 VertexCoordinates;

out vec4 FragColor;

void main() {
    FragColor = texture(Texture0, VertexCoordinates);
}