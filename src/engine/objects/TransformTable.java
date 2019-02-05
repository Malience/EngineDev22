package engine.objects;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.StructBuffer;

import math.Matrix4f;
import math.Matrix4f.Buffer;
import math.Vector3f;

public class TransformTable {
	private static final int CAPACITY = 30;
	
	private static final Vector3f.Buffer position;
	private static final Vector3f.Buffer rotation;
	private static final Vector3f.Buffer scale;
	
	private static final Matrix4f.Buffer transform;
	
	static {
		position = Vector3f.malloc(CAPACITY);
		rotation = Vector3f.malloc(CAPACITY);
		scale = Vector3f.malloc(CAPACITY);
		
		transform = Matrix4f.malloc(CAPACITY);
	}
	
	public static void create(long id) {
		
	}
}
