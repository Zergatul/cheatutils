#version 330

layout(std140) uniform Block {
    mat4 MVP;
};

in vec3 Position;

void main() {
    gl_Position = MVP * vec4(Position, 1.0);
}