package api.vulkan;

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
}
