#version 150

uniform float Feather;

in vec4 Color;
in float FragGradient;
in float FragLineWidth;

out vec4 FragColor;

void main() {
    float distance = abs(FragGradient - 0.5) * (FragLineWidth + Feather);
    float alpha = smoothstep((FragLineWidth - Feather) / 2, (FragLineWidth + Feather) / 2, distance);
    FragColor = vec4(Color.rgb, Color.a * (1 - alpha));
}