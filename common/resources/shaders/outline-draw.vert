#version 150

in vec3 InPosition;
in vec2 InTexCoords;

out vec2 TexCoords;

void main() {
    gl_Position = vec4(InPosition, 1.0);
    TexCoords = InTexCoords;
}