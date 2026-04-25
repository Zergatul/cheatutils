#version 330

layout(std140) uniform Inputs {
    mat4 MVP;
};

in vec3 inOrigin;

const vec3 CORNERS[8] = vec3[](
    vec3(0.0, 0.0, 0.0),
    vec3(1.0, 0.0, 0.0),
    vec3(1.0, 0.0, 1.0),
    vec3(0.0, 0.0, 1.0),
    vec3(0.0, 1.0, 0.0),
    vec3(1.0, 1.0, 0.0),
    vec3(1.0, 1.0, 1.0),
    vec3(0.0, 1.0, 1.0)
);

const int INDICES[36] = int[](
    0, 2, 3, 0, 1, 2,
    4, 7, 6, 4, 6, 5,
    0, 4, 5, 0, 5, 1,
    3, 6, 7, 3, 2, 6,
    0, 3, 7, 0, 7, 4,
    1, 5, 6, 1, 6, 2
);

void main() {
    vec3 position = inOrigin + CORNERS[INDICES[gl_VertexID]];
    gl_Position = MVP * vec4(position, 1.0);
}
