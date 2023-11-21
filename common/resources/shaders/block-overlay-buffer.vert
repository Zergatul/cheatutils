#version 150

in vec3 Position;

uniform mat4 ModelView;
uniform mat4 Projection;

void main() {
    gl_Position = Projection * ModelView * vec4(Position, 1.0);
}