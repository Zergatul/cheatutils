#version 150

in vec3 Position;

uniform mat4 MVP;
uniform mat4 Projection;

void main() {
    gl_Position = MVP * vec4(Position, 1.0);
}