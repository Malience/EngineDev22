#version 450
#extension GL_ARB_separate_shader_objects : enable

layout(location = 0) in vec3 pos;
layout(location = 1) in vec3 normal;
layout(location = 2) in vec2 texCoord;

layout(location = 0) out vec3 Pos;
layout(location = 1) out vec3 Normal;
layout(location = 2) out vec2 TexCoord;

layout(binding = 0) uniform CameraObject {
	mat4 proj;
	mat4 view;
	mat4 model;
};

void main() {
    gl_Position = proj * view * model * vec4(pos, 1.0);
    Pos = pos;
    Normal = normal;
    TexCoord = texCoord;
}