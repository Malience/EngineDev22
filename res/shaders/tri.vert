#version 450
#extension GL_ARB_separate_shader_objects : enable

layout(location = 0) in vec3 pos;
layout(location = 1) in vec3 normal;
layout(location = 2) in vec2 texCoord;

layout(location = 0) out vec3 Pos;
layout(location = 1) out vec3 Normal;
layout(location = 2) out vec2 TexCoord;

layout(binding = 0) uniform ViewProjectionBuffer {
	mat4 view;
	mat4 proj;
};

layout(binding = 1) uniform ModelBuffer {
	mat4 model;
};

void main() {
	gl_Position = proj * view * model * vec4(pos, 1.0);
    Pos = vec3(model * vec4(pos, 1.0));//Into World space
    Normal = mat3(transpose(inverse(model))) * normal; //In weird space
    TexCoord = texCoord;
}