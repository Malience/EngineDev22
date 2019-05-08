package math;

public class Transform3D {
	//~~~~~~~~~~~FIELDS~~~~~~~~~~~\\
	private Transform3D parent;
	//private ArrayList<Transform3D> children;
	
	private Matrix4f transform;
	
	private Vector3f pos;
	private Quaternion rot;
	private Vector3f scale;
	
	private boolean changed;
	
	//~~~~~~~~~~~CONSTRUCTORS~~~~~~~~~~~\\
	
	public Transform3D() {
		pos = new Vector3f();
		rot = new Quaternion();
		scale = new Vector3f(1,1,1);
		
		transform = new Matrix4f();
	}
	
	public Transform3D(Matrix4f.Buffer buffer) {
		pos = new Vector3f();
		rot = new Quaternion();
		scale = new Vector3f(1,1,1);
		
		transform = buffer.get();
		makeTransformation();
	}
	
	//~~~~~~~~~~~SETTERS~~~~~~~~~~~\\
	
	public Matrix4f transform(Matrix4f out) {
		return out.translationRotateScale(pos, rot, scale);
	}
	
	public Matrix4f inverse(Matrix4f out) {
		return out.inverseTRS(pos, rot, scale);
	}
	
	public void setParent(Transform3D parent) {this.parent = parent;}
	
	public void translate(Vector3f v) {pos.add(v); changed = true;}
	public void translate(float x, float y, float z) {pos.add(x, y, z); changed = true;}
	
	public void rotate(Quaternion q) {rot.mul(q, rot); changed = true;}
	public void rotate(Vector3f v, float a) {
		Quaternion q = new Quaternion();
		q.axisAngle(v, a);
		rotate(q);
	}
	public void rotate(float x, float y, float z) {
		Quaternion q = new Quaternion();
		q.euler(x, y, z);
		rotate(q);
	}
	
	public void scale(float x, float y, float z) {
		scale.mul(x, y, z);
		changed = true;
	}
	
	
	//~~~~~~~~~~~GETTERS~~~~~~~~~~~\\
	
	private void makeTransformation() {
		//if(!changed) return;
		transform.translationRotateScale(pos, rot, scale);
		if(parent != null) parent.transform().mul(transform, transform);
		changed = false;
	}
	
	public Matrix4f transform() {
		makeTransformation();
		return transform;
	}
	
	public String toString() {
		return
				"Position: \t" + pos.x() + ", " + pos.y() + ", " + pos.z() + "\n" +
				"Rotation: \t" + rot.x() + ", " + rot.y() + ", " + rot.z() + ", " + rot.w() + "\n" +
				"Scale: \t\t" + scale.x() + ", " + scale.y() + ", " + scale.z();
	}
}
