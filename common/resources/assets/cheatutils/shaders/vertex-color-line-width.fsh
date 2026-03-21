#version 330

layout(std140) uniform Block {
    float Feather;
};

in vec4 VertexColor;
in float VertexGradient;
in float VertexLineWidth;

out vec4 FragColor;

void main() {
    float distance = abs(VertexGradient - 0.5) * (VertexLineWidth + Feather);
    float alpha = smoothstep((VertexLineWidth - Feather) / 2, (VertexLineWidth + Feather) / 2, distance);
    FragColor = vec4(VertexColor.rgb, VertexColor.a * (1 - alpha));
}