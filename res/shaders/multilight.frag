#version 450
#extension GL_ARB_separate_shader_objects : enable

layout(location = 0) in vec3 Pos;
layout(location = 1) in vec3 Normal;
layout(location = 2) in vec2 TexCoord;
layout(location = 3) flat in uint instanceIndex;

layout(location = 0) out vec4 FragColor;

const int MAX_NUM_OBJECTS = 16;

layout(binding = 0) uniform ViewProjectionBuffer {
	mat4 view;
	mat4 proj;
};

struct Material {
	vec4 ambient;
	vec4 diffuse; //Technically rgba, mainly vec4 for alignment reasons
	vec4 specular; //a == specular exp coeff
	vec4 emissive;
};

layout(binding = 2) uniform MaterialBuffer {
	Material[16] material;
};

struct DirectionalLight {
	vec3 dir;
	vec3 color;
};

layout(binding = 3, std140) uniform DirectionalLightBuffer {
	DirectionalLight dlight;
};

struct PointLight {
	vec3 pos;
	vec3 color;
};

layout(binding = 4, std140) uniform PointLightBuffer {
	PointLight[2] plights;
};

layout(binding = 5, std140) uniform MaterialIndicesBuffer {
	int materialIndices[MAX_NUM_OBJECTS];
};
layout(binding = 6, std140) uniform TextureIndicesBuffer {
	int textureIndices[MAX_NUM_OBJECTS];
};

layout(binding = 7) uniform sampler textureSampler;
layout(binding = 8) uniform texture2D textures[8];

layout(push_constant) uniform fConstants {
	layout(offset = 4) uint numPointLights;
	layout(offset = 8) uint numSpotLights; //TODO: Spotlights
};

float ambientLighting = 0.3;
float specularLighting = 0.5;

vec3 processDirectionalLight(DirectionalLight light, vec3 pos, vec3 norm, vec3 eyeDir, Material mat, vec3 color) {
	vec3 lightDir = light.dir;
	vec3 lightColor = light.color;
	
	//Ambient
	vec3 ambient = ambientLighting * lightColor * vec3(mat.ambient);
	
	//Diffuse
	float dirDot = max(dot(norm, lightDir), 0.0);
	vec3 diffuse = dirDot * lightColor * vec3(mat.diffuse.rgb * color);
	
	//Specular
	vec3 specular = vec3(0);
	if(mat.specular.a > 0) {
		vec3 reflectDir = normalize(reflect(-lightDir, norm));
		float s = pow(max(dot(eyeDir, reflectDir), 0.0), mat.specular.a);
		specular = specularLighting * s * lightColor * mat.specular.rgb;
	}
	
	return ambient + diffuse + specular;
}

vec3 processPointLight(PointLight light, vec3 pos, vec3 norm, vec3 eyeDir, Material mat, vec3 color) {
	vec3 lightPos = (view * vec4(light.pos, 1)).xyz;
	vec3 lightColor = light.color;
	
	//Ambient
	vec3 ambient = ambientLighting * lightColor * vec3(mat.ambient);
	
	//Diffuse
	vec3 lightDir = normalize(lightPos - pos);
	float dirDot = max(dot(norm, lightDir), 0.0);
	vec3 diffuse = dirDot * lightColor * vec3(mat.diffuse.rgb * color);
	
	//Specular
	vec3 specular = vec3(0);
	if(mat.specular.a > 0) {
		vec3 eyeDir = normalize(-Pos); //Advantages of eye space
		vec3 reflectDir = normalize(reflect(-lightDir, norm));
		float s = pow(max(dot(eyeDir, reflectDir), 0.0), mat.specular.a);
		specular = specularLighting * s * lightColor * mat.specular.rgb;
	}
	
	return ambient + diffuse + specular;
}

void main() {
	Material mat = material[materialIndices[instanceIndex]];
	vec3 norm = normalize(Normal);
	vec3 eyeDir = normalize(-Pos); //Advantages of eye space
	
	int textureIndex = textureIndices[instanceIndex];
	
	vec3 color = vec3(0);
	if(textureIndex >= 0) color = vec3(texture(sampler2D(textures[textureIndex], textureSampler), TexCoord));
	else color = vec3(1);
	
    
    vec3 outColor = processDirectionalLight(dlight, Pos, norm, eyeDir, mat, color);
    
    for(int i = 0; i < numPointLights; i++) {
    	outColor = outColor + processPointLight(plights[i], Pos, norm, eyeDir, mat, color);
    }
    
    outColor = outColor + mat.emissive.rgb;
    FragColor = vec4(outColor, 1.0);
}