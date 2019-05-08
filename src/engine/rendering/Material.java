package engine.rendering;

import math.Matrix4f;
import math.Vector4f;

public class Material {
	Vector4f.Buffer color;
	
	public Material() {
		color = Vector4f.calloc(4);
	}
	
	public Material setAmbientColor(float r, float g, float b, float a) {
		color.get(0).x(r).y(g).z(b).w(a);
		return this;
	}
	
	public Material setDiffuseColor(float r, float g, float b, float a) {
		color.get(1).x(r).y(g).z(b).w(a);
		return this;
	}
	
	public Material setSpecularColor(float r, float g, float b, float a) {
		color.get(2).x(r).y(g).z(b).w(a);
		return this;
	}
	
	public Material setEmissiveColor(float r, float g, float b, float a) {
		color.get(3).x(r).y(g).z(b).w(a);
		return this;
	}
	
	//TODO: Textures
	
	public String toString() {
		return 
				"Ambient: \t" + color.get(0) + "\n" +
				"Diffuse: \t" + color.get(1) + "\n" +
				"Specular: \t" + color.get(2) + "\n" +
				"Emissive: \t" + color.get(3);
	}
}
