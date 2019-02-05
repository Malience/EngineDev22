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
		q.close();
	}
	
	//~~~~~~~~~~~GETTERS~~~~~~~~~~~\\
	
	private void makeTransformation() {
		if(!changed) return;
		transform.translationRotateScale(pos, rot, scale);
		if(parent != null) transform.mul(parent.transform(), transform);
		changed = false;
	}
	
	public Matrix4f transform() {
		makeTransformation();
		return transform;
	}
	
	
}
