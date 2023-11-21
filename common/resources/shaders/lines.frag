#version 150

in vec4 Color;
out vec4 FragColor;

void main() {
    //FragColor = vec4(0.5, 0.6, 0.7, 0.8);
    FragColor = Color;
}