package engine.rendering;

import math.Vector3f;

public class PointLight {
	public Vector3f pos;
	public Vector3f color;
	
	public PointLight() {
		pos = new Vector3f();
		color = new Vector3f();
	}
	
	//TODO: Attentuation;
	//TODO: Buffers and stack allocation shenanigans
}
