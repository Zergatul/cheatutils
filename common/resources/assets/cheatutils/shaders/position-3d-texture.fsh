#version 330

uniform sampler2D InSampler;

in vec2 VertexCoordinates;

out vec4 FragColor;

void main() {
    FragColor = texture(InSampler, VertexCoordinates);
}