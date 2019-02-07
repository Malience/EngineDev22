#version 450
#extension GL_ARB_separate_shader_objects : enable

layout(location = 0) in vec3 Pos;
layout(location = 1) in vec3 Normal;
layout(location = 2) in vec2 TexCoord;

layout(location = 0) out vec4 FragColor;

layout(binding = 2) uniform TextureBuffer { //Because these would be textures in more complex shaders
	vec4 diffuse; //Technically rgba, mainly vec4 for alignment reasons
	vec4 ambient;
	vec4 specular; //a == specular coeff
	vec4 emissive;
} object;

layout(binding = 3) uniform LightBuffer {
	vec4 pos;
	vec4 color;
	vec4 eyePos;
} light;

void phongShade() {
	//Multiple lights support
}

void main() {
	float ambientLighting = 0.1;
	float specularLighting = 0.5;
	
	vec3 normal = normalize(Normal);
	vec3 lightDir = normalize(light.pos.xyz - Pos);
	float dirDot = dot(lightDir, normal);
	
	vec3 eyeDir = normalize(eyePos - Pos);
	vec3 reflectDir = reflect(-lightDir, normal);
	float s = pow(dot(viewDir, reflectDir), 32);
	
	vec4 outColor = vec4(0);
	if(dirDot > 0.0) {
		vec4 ambient = light.color * ambientLighting * object.ambient;
		vec4 diffuse = light.color * dirDot * object.diffuse;
		vec4 specular = specularLighting * s * light.color * object.specular;
		
		outColor = outColor + ambient + diffuse + specular;
	}
	outColor.a = 1.0;
	//vec4 outColor = diffuse;
    FragColor = outColor;
}