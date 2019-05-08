package engine.rendering;

import math.Vector3f;

public class DirectionalLight {
	public Vector3f dir;
	public Vector3f color;
	
	public DirectionalLight() {
		dir = new Vector3f();
		color = new Vector3f();
	}
	
	//TODO: Buffers and stack allocation shenanigans
}
