package primitives;

public class KDOP extends Primitive {
	float[] max, min;
	public KDOP(int k) {
		super(Primitive.KDOP); 
		k >>= 1;
		max = new float[k];
		min = new float[k];
	}
}
