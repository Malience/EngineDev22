#version 450
#extension GL_ARB_separate_shader_objects : enable

layout(location = 0) in vec3 Pos;
layout(location = 1) in vec3 Normal;
layout(location = 2) in vec2 TexCoord;
layout(location = 3) flat in uint instanceIndex;

layout(location = 0) out vec4 FragColor;

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

layout(binding = 2) uniform TextureBuffer { //Because these would be textures in more complex shaders
	Material[96] material;
};

layout(binding = 3) uniform LightBuffer {
	vec4 pos;
	vec4 color;
	vec4 eyePos;
} light;

void main() {
	Material mat = material[instanceIndex];
	vec3 objPos = vec3(Pos);
	vec3 norm = normalize(Normal);
	vec3 lightPos = vec3(light.pos);
	vec3 lightColor = vec3(light.color);
	
	//mat4 iView = transpose(inverse(view));
	//norm = (iView * vec4(norm, 0)).xyz;
	lightPos = (view * vec4(lightPos, 1)).xyz;
	//objPos = (view * vec4(objPos, 1)).xyz;
	
	//Ambient
	float ambientLighting = 0.3;
	vec3 ambient = ambientLighting * lightColor * vec3(mat.ambient);
	
	//Diffuse
	vec3 lightDir = normalize(lightPos - Pos);
	float dirDot = max(dot(norm, lightDir), 0.0);
	vec3 diffuse = dirDot * lightColor * vec3(mat.diffuse);
	
	vec3 specular = vec3(0);
	//Specular
	if(mat.specular.a > 0) {
		float specularLighting = 0.5;
		vec3 eyeDir = normalize(-Pos); //Advantages of eye space
		vec3 reflectDir = normalize(reflect(-lightDir, norm));
		float s = pow(max(dot(eyeDir, reflectDir), 0.0), mat.specular.a);
		specular = specularLighting * s * lightColor * mat.specular.rgb;
	}
	
	vec3 outColor = ambient + diffuse + specular + mat.emissive.rgb;
    FragColor = vec4(outColor, 1.0);
}