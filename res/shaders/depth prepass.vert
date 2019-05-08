#version 450
#extension GL_ARB_separate_shader_objects : enable

layout(location = 0) in vec3 pos;
layout(location = 1) in vec3 normal;
layout(location = 2) in vec2 texCoord;

layout(binding = 0) uniform ViewProjectionBuffer {
	mat4 view;
	mat4 proj;
};

layout(binding = 1) uniform ModelBuffer {
	mat4[96] model;
};

layout(push_constant) uniform constants {
	uint instanceOffset;
};

void main() {
	uint instanceIndex = instanceOffset + gl_InstanceIndex;
	mat4 Model = model[instanceIndex];
	gl_Position = proj * view * Model * vec4(pos, 1.0);
}