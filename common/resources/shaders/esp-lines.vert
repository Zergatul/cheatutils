#version 150

in vec3 Position;
in vec4 InColor;

uniform mat4 MVP;

out vec4 Color;

void main() {
    gl_Position = MVP * vec4(Position, 1.0);
    Color = InColor;
}