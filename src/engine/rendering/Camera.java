package engine.rendering;

import java.nio.DoubleBuffer;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.system.MemoryStack;

import api.vulkan.BufferObject;
import engine.core.Time;
import engine.window.Window;
import math.Constants;
import math.Matrix4f;
import math.Quaternion;
import math.Vector3f;
import math.Vector4f;

public class Camera {
	Matrix4f.Buffer viewProjection;
	Matrix4f view, projection;
	Vector3f pos;
	Quaternion rot = new Quaternion();
	float rotx, roty, origx, origy, centerx, centery;
	float distance = -12;
	float speed = 6f;
	boolean held;
	
	public Camera() {
		viewProjection = Matrix4f.calloc(2);
		view = viewProjection.get(0).identity();
		projection = viewProjection.get(1).identity();
		pos = new Vector3f(0, 0, distance);
	}
	
	public Camera setPerspective(float fov, float width, float height, float near, float far) {
		projection.perspective(90f, width / height, near, far);
		centerx = width / 2; centery = height / 2;
		return this;
	}
	
	public void update(Window window) {
		try(MemoryStack stack = MemoryStack.stackPush()) {
			if(		GLFW.glfwGetMouseButton(window.getHandle(), GLFW.GLFW_MOUSE_BUTTON_2) == GLFW.GLFW_PRESS) {
				DoubleBuffer x = stack.mallocDouble(1), y = stack.mallocDouble(1);
				GLFW.glfwGetCursorPos(window.getHandle(), x, y);
				GLFW.glfwSetCursorPos(window.getHandle(), centerx, centery);
				if(!held) {
					GLFW.glfwSetInputMode(window.getHandle(), GLFW.GLFW_CURSOR, GLFW.GLFW_CURSOR_HIDDEN);
					origx = (float)x.get(0); origy = (float)y.get(0);
					held = true;
				} else {
					Quaternion q = new Quaternion();
					float rotSpeed = 5;
					rotx += (((float) x.get(0)) - centerx) * rotSpeed * Time.getDelta();
					roty += (((float) y.get(0)) - centery) * rotSpeed * Time.getDelta();
					if(roty > 89) roty = 89;
					if(roty < -89) roty = -89;
					//pos.set(0, 0, distance);
					rot.axisAngle(1, 0, 0, roty * Constants.RADIAN).mul(q.axisAngle(0, 1, 0, rotx * Constants.RADIAN), rot);
					rot.normalize();
					//view.setTranslation(pos);
				}
				
			}
			if(GLFW.glfwGetMouseButton(window.getHandle(), GLFW.GLFW_MOUSE_BUTTON_2) == GLFW.GLFW_RELEASE && held) {
				GLFW.glfwSetInputMode(window.getHandle(), GLFW.GLFW_CURSOR, GLFW.GLFW_CURSOR_NORMAL);
				GLFW.glfwSetCursorPos(window.getHandle(), origx, origy);
				held = false;
			}
			if(GLFW.glfwGetKey(window.getHandle(), GLFW.GLFW_KEY_W) == GLFW.GLFW_PRESS) {
				Vector3f v = new Vector3f(view.m20() * speed * Time.getDelta(), view.m21() * speed * Time.getDelta(), view.m22() * speed * Time.getDelta());
				pos.add(v, pos);
			}
			if(GLFW.glfwGetKey(window.getHandle(), GLFW.GLFW_KEY_S) == GLFW.GLFW_PRESS) {
				Vector3f v = new Vector3f(view.m20() * -speed * Time.getDelta(), view.m21() * -speed * Time.getDelta(), view.m22() * -speed * Time.getDelta());
				pos.add(v, pos);
			}
			if(GLFW.glfwGetKey(window.getHandle(), GLFW.GLFW_KEY_A) == GLFW.GLFW_PRESS) {
				Vector3f v = new Vector3f(view.m00() * speed * Time.getDelta(), view.m01() * speed * Time.getDelta(), view.m02() * speed * Time.getDelta());
				pos.add(v, pos);
			}
			if(GLFW.glfwGetKey(window.getHandle(), GLFW.GLFW_KEY_D) == GLFW.GLFW_PRESS) {
				Vector3f v = new Vector3f(view.m00() * -speed * Time.getDelta(), view.m01() * -speed * Time.getDelta(), view.m02() * -speed * Time.getDelta());
				pos.add(v, pos);
			}
			if(GLFW.glfwGetKey(window.getHandle(), GLFW.GLFW_KEY_LEFT_CONTROL) == GLFW.GLFW_PRESS) {
				Vector3f v = new Vector3f(view.m10() * speed * Time.getDelta(), view.m11() * speed * Time.getDelta(), view.m12() * speed * Time.getDelta());
				pos.add(v, pos);
			}
			if(GLFW.glfwGetKey(window.getHandle(), GLFW.GLFW_KEY_LEFT_SHIFT) == GLFW.GLFW_PRESS) {
				Vector3f v = new Vector3f(view.m10() * -speed * Time.getDelta(), view.m11() * -speed * Time.getDelta(), view.m12() * -speed * Time.getDelta());
				pos.add(v, pos);
			}
			view.setRotation(rot);
			view.translate(pos);
		}
	}
}
