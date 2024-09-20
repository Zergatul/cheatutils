#version 150

in vec3 Position;
in vec4 InColor;
in float Gradient;
in float LineWidth;

out vec4 Color;
out float FragGradient;
out float FragLineWidth;

void main() {
    gl_Position = vec4(Position, 1.0);
    Color = InColor;
    FragGradient = Gradient;
    FragLineWidth = LineWidth;
}