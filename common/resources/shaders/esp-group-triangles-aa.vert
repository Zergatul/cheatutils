#version 150

in vec3 Position;
in float Gradient;

out float FragGradient;

void main() {
    gl_Position = vec4(Position, 1.0);
    FragGradient = Gradient;
}