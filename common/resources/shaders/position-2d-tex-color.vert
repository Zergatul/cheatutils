#version 150

in vec2 InPosition;
in vec2 InTexCoords;
in vec4 InColor;

uniform mat4 MVP;

out vec2 TexCoords;
out vec4 Color;

void main() {
    gl_Position = MVP * vec4(InPosition, 0.0, 1.0);
    TexCoords = InTexCoords;
    Color = InColor;
}