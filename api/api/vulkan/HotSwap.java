package api.vulkan;

import java.nio.DoubleBuffer;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import engine.core.Time;
import math.Constants;
import math.Matrix4f;
import math.Quaternion;

public class HotSwap {
	static Matrix4f.Buffer camera;
	static Matrix4f persp, view, model;
	static Quaternion rot;
	static {
		camera = Matrix4f.calloc(3);
		persp = camera.get(0).identity(); view = camera.get(1).identity(); model = camera.get(2).identity();
		rot = new Quaternion();
	}
	static float time;
	public static void matrixUpload(long pointer, int size, Quaternion q) {
		float near = 0.1f, far = 40f;
		float aspect = 800f / 600f;
		
		persp.perspective(90f, aspect, near, far);
		
		//float speed = 0.3f;
		//time = rot.x() + Time.getDelta() * 2 * Constant.PI * speed;
		//if(time >= 2 * Constant.PI) time -= 2 * Constant.PI;
		//rot.x(time);
		
		//view.setRotation(0, 0, 1, time);
		view.setRotation(q);
		view.translation(0, 0, -4f);
		
		model.set(
				1f, 0f, 0f, 0f,
				0f, 1f, 0f, 0f,
				0f, 0f, 1f, 0f,
				0f, 0f, 0f, 1f);
		
		MemoryUtil.memCopy(camera.address(), pointer, size);
	}
	static boolean held = false;
	static float centerx = 400f, centery = 300f, rotx = 0, roty = 0, origx, origy, distance = -8f;
	public static void rotate(BufferObject viewProjectionBuffer, Matrix4f.Buffer viewProjection, Matrix4f proj, Matrix4f view) {
		try(MemoryStack stack = MemoryStack.stackPush()) {
			float fov = 45;
			float near = 0.01f, far = 30f;
			float aspect = 800f/600f;
			float tanFov2 = 1.0f / (float)Math.tan((fov * 0.5f) * Constants.RADIAN), 
					range = 1.0f / (near - far);
			proj.set(
					tanFov2 / aspect, 0f, 0f, 0f,
					0f, -tanFov2, 0f, 0f,
					0f, 0f, far * range, far * near * range,
					0f, 0f, -1.0f, 0f,
					0);
			//rotx = 0f;
			rotx += Time.getDelta() * .3f;
			view.set(	1, 0, 0, (float)Math.sin(rotx) * 4, 
						0, 1, 0, 0, 
						0, 0, 1, 0, 
						0, 0, 0, 1,
				0);
			
			
			viewProjectionBuffer.map(viewProjection);
		}
	}
}
