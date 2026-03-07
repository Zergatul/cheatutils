#version 330

layout(std140) uniform Block {
    vec4 Color;
    float LineWidth;
    float Feather;
};

in float FragGradient;

out vec4 FragColor;

void main() {
    float distance = abs(FragGradient - 0.5) * (LineWidth + Feather);
    float alpha = smoothstep((LineWidth - Feather) / 2, (LineWidth + Feather) / 2, distance);
    FragColor = vec4(Color.rgb, Color.a * (1 - alpha));
}