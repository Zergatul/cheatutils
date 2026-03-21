#version 330

in vec3 Position;
in vec4 Color;
in float Gradient;
in float LineWidth;

out vec4 VertexColor;
out float VertexGradient;
out float VertexLineWidth;

void main() {
    gl_Position = vec4(Position, 1.0);
    VertexColor = Color;
    VertexGradient = Gradient;
    VertexLineWidth = LineWidth;
}