#version 150

in vec3 Position;

uniform mat4 Projection;

out vec4 Color;

void main() {
    gl_Position = Projection * vec4(Position, 1.0);
    Color = vec4(1.0, 1.0, 1.0, 1.0);
}