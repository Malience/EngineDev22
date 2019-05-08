#version 450
#extension GL_ARB_separate_shader_objects : enable

layout(location = 0) in vec3 pos;
layout(location = 1) in vec3 normal;
layout(location = 2) in vec2 texCoord;

layout(location = 0) out vec3 Pos;
layout(location = 1) out vec3 Normal;
layout(location = 2) out vec2 TexCoord;
layout(location = 3) flat out uint instanceIndex;

layout(binding = 0) uniform ViewProjectionBuffer {
	mat4 view;
	mat4 proj;
};

layout(binding = 1) uniform ModelBuffer {
	mat4[16] model;
};

layout(push_constant) uniform vConstants {
	layout(offset = 0) uint instanceOffset;
};

void main() {
	instanceIndex = instanceOffset + gl_InstanceIndex;
	mat4 Model = model[instanceIndex];
	gl_Position = proj * view * Model * vec4(pos, 1.0);
    Pos = (view * Model * vec4(pos, 1.0)).xyz;//Into Eye space
    Normal = normalize(transpose(inverse(mat3(view * Model))) * normal); //In Eye space
    TexCoord = texCoord;
}