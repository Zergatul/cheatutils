#version 150

in vec3 Position;
in vec4 InColor;

out vec4 Color;

void main() {
    gl_Position = vec4(Position, 1.0);
    Color = InColor; //vec4(InColor, 1.0);
}