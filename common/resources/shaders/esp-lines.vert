#version 150

in vec3 Position;
in vec4 InColor;

uniform mat4 ModelView;
uniform mat4 Projection;

out vec4 Color;

void main() {
    gl_Position = Projection * ModelView * vec4(Position, 1.0);
    Color = InColor;
}