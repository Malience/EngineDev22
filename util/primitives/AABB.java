package primitives;

import math.Vector3f;

public class AABB extends Primitive {
	public Vector3f pos;
	public float hx, hy, hz;
	public AABB(){this(new Vector3f(0f, 0f, 0f), 1f, 1f, 1f);}
	public AABB(float x, float y, float z, float h) {this(new Vector3f(x, y, z), h, h, h);}
	public AABB(float x, float y, float z, float hx, float hy, float hz) {this(new Vector3f(x, y, z), hx, hy, hz);}
	public AABB(Vector3f pos, float h) {this(pos, h, h, h);}
	public AABB(Vector3f pos, float hx, float hy, float hz) {super(Primitive.AABB); this.pos = pos; this.hx = hx; this.hy = hy; this.hz = hz;}
	
	public float volume() {return hx * hy * hz * 2;}
	
	public String toString(){return "Pos: " + pos+ "\t Halfwidths: " + hx + ", " + hy + ", " + hz;}
}
