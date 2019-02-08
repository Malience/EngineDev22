#version 450
#extension GL_ARB_separate_shader_objects : enable

layout(location = 0) in vec3 Pos;
layout(location = 1) in vec3 Normal;
layout(location = 2) in vec2 TexCoord;

layout(location = 0) out vec4 FragColor;

layout(binding = 0) uniform ViewProjectionBuffer {
	mat4 view;
	mat4 proj;
};

layout(binding = 2) uniform TextureBuffer { //Because these would be textures in more complex shaders
	vec4 ambient;
	vec4 diffuse; //Technically rgba, mainly vec4 for alignment reasons
	vec4 specular; //a == specular exp coeff
	vec4 emissive;
} object;

layout(binding = 3) uniform LightBuffer {
	vec4 pos;
	vec4 color;
	vec4 eyePos;
} light;

void main() {
	vec3 normal = normalize(Normal);
	vec3 lightPos = vec3(light.pos);
	vec3 lightColor = vec3(light.color);
	
	//Ambient
	float ambientLighting = 0.3;
	vec3 ambient = ambientLighting * lightColor * vec3(object.ambient);
	
	//Diffuse
	vec3 lightDir = normalize(lightPos - Pos);
	float dirDot = max(dot(normal, lightDir), 0.0);
	vec3 diffuse = dirDot * lightColor * vec3(object.diffuse);
	
	//Specular
	float specularLighting = 0.5;
	vec3 eyeDir = normalize(light.eyePos.xyz - Pos);
	vec3 reflectDir = reflect(-lightDir, normal);
	float s = pow(dot(eyeDir, reflectDir), object.specular.a);
	vec3 specular = specularLighting * s * lightColor * object.specular.rgb;
	
	vec3 outColor = ambient + diffuse + object.emissive.rgb;
	if(specular.r + specular.g + specular.b > 0.0) outColor = outColor + specular;
    FragColor = vec4(outColor, 1.0);
}