package primitives;

public class Primitive {
	public static final byte SPHERE		 	= 1;
	public static final byte PLANE 			= 2;
	public static final byte AABB		 	= 3;
	public static final byte CAPSULE	 	= 4;
	public static final byte OBB 			= 5;
	public static final byte KDOP		 	= 6;
	
	public static final byte SLAB 			= 8;
	public static final byte SSAABB 		= 9;
	public static final byte LOZENGE 		= 13;
	
	public static final byte LINE 			= 15;
	public static final byte LINE_SEGMENT 	= 16;
	public static final byte RAY 			= 17;
	public static final byte TRIANGLE 		= 10;
	
	public final byte type;
	public Primitive(byte type) {this.type = type;}
}
