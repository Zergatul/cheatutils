#version 150

in vec3 InPosition;
in vec2 InTexCoords;

uniform mat4 MVP;

out vec2 TexCoords;

void main() {
    gl_Position = MVP * vec4(InPosition, 1.0);
    TexCoords = InTexCoords;
}