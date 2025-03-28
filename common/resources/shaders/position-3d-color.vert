#version 150

in vec3 InPosition;
in vec4 InColor;

uniform mat4 MVP;

out vec4 Color;

void main() {
    gl_Position = MVP * vec4(InPosition, 1.0);
    Color = InColor;
}