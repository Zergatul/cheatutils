#version 330

layout(std140) uniform Inputs {
    mat4 MVP;
};

in vec3 InPosition;
in vec2 InTexCoords;

out vec2 TextureCoordinates;

void main() {
    gl_Position = MVP * vec4(InPosition, 1.0);
    TextureCoordinates = InTexCoords;
}