#version 150

uniform mat4 MVP;

in vec3 inPosition;
in vec2 inTexCoords;

out vec2 texCoords;

void main() {
    gl_Position = MVP * vec4(inPosition, 1.0);
    texCoords = inTexCoords;
}