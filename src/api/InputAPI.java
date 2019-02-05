package api;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.vulkan.VK10.VK_TRUE;
import static org.lwjgl.vulkan.VK10.VK_FALSE;

import org.lwjgl.glfw.GLFWErrorCallbackI;
import org.lwjgl.glfw.GLFWWindowCloseCallbackI;
import org.lwjgl.system.MemoryUtil;

import engine.debug.Debug;
import engine.multithread.Threads;

public abstract class InputAPI {
	private static boolean initialized = false;
	
	public static final int ERROR_CALLBACK = 2000,
							WINDOW_SIZE_CALLBACK = 2001;
	
	public static final int CONTEXT_VERSION_MAJOR = GLFW_CONTEXT_VERSION_MAJOR,
							CONTEXT_VERSION_MINOR = GLFW_CONTEXT_VERSION_MINOR,
							OPENGL_PROFILE = GLFW_OPENGL_PROFILE,
							OPENGL_CORE_PROFILE = GLFW_OPENGL_CORE_PROFILE,
							VISIBLE = GLFW_VISIBLE,
							RESIZABLE = GLFW_RESIZABLE,
							TRUE = VK_TRUE,
							FALSE = VK_FALSE,
							CURSOR = GLFW_CURSOR,
							CURSOR_DISABLED = GLFW_CURSOR_DISABLED,
							CURSOR_NORMAL = GLFW_CURSOR_NORMAL;
	
	//MODS
	public static final int MOD_SHIFT	= 0x1,
							MOD_CTRL	= 0x2,
							MOD_ALT		= 0x4,
							MOD_SUPER 	= 0x8;
	
	//ACTIONS
	public static final int UNKNOWN = 0x0,
							PRESS	= 0x1,
							REPEAT	= 0x3,
							RELEASE	= 0x2,
							HOLD	= REPEAT;
	
	//KEY CODES
	public static final int 
	KEY_SPACE 	= GLFW_KEY_SPACE,				
	KEY_ESCAPE	= GLFW_KEY_ESCAPE,
	KEY_W	= GLFW_KEY_W,
	KEY_A	= GLFW_KEY_A,
	KEY_S	= GLFW_KEY_S,
	KEY_D	= GLFW_KEY_D;
	
	public static void init() {
		if(initialized) {
			Debug.warn("API", "Input already initialized!");
			return;
		}
		InputAPI.setDefaultErrorCallback();
		if(glfwInit() && Threads.isMainThread()) {
			initialized = true;
			Debug.log("API", "Input initialized"); 
		} else {
			Debug.error("API", "Input initialization failed!"); 
		}
	}
	public static void dispose() {
		if(!initialized) {
			Debug.warn("API", "Input is not initialized!");
			return;
		}
		glfwTerminate();
		initialized = false;
		Debug.log("API", "Input disposed");
	}
	
	public static void defaultWindowHints() {glfwDefaultWindowHints();}
	public static void windowHint(int hint, int value) {glfwWindowHint(hint, value);}
	
	public static long createWindow(int width, int height, CharSequence title) {return createWindow(width, height, title, 0L, 0L);}
	public static long createWindow(int width, int height, CharSequence title, long monitor, long share) {return glfwCreateWindow(width, height, title, monitor, share);}
	
	public static void swapInterval(int interval) {glfwSwapInterval(interval);}
	public static void swapBuffers(long window) {glfwSwapBuffers(window);}
	
	public static void makeContextCurrent(long window) {glfwMakeContextCurrent(window);}
	public static void showWindow(long window) {glfwShowWindow(window);}
	public static void hideWindow(long window) {glfwHideWindow(window);}
	public static void destroyWindow(long window) {glfwDestroyWindow(window);}
	public static boolean windowShouldClose(long window) {return glfwWindowShouldClose(window);}
	
	public static void setInputMode(long window, int mode, int value) {glfwSetInputMode(window, mode, value);}
	public static void disableCursor(long window) {glfwSetInputMode(window, CURSOR, CURSOR_DISABLED);}
	public static void enableCursor(long window) {glfwSetInputMode(window, CURSOR, CURSOR_NORMAL);}
	
	public static void setCursorPos(long window, int x, int y) {glfwSetCursorPos(window, x, y);}
	public static void setCursorPos(long window, float x, float y) {glfwSetCursorPos(window, x, y);}
	
	public static void pollEvents() {glfwPollEvents();}
	
	public static void setErrorCallback(ErrorCallback callback) {glfwSetErrorCallback(callback);}
	public static void setDefaultErrorCallback() {
		setErrorCallback((int error, long desc) -> {
			String s = MemoryUtil.memUTF8(desc);
			switch(error) {
			case GLFW_NOT_INITIALIZED: 		Debug.error("API", "GLFW not initialized!" /*\n\t" + s*/); return;
			case GLFW_NO_CURRENT_CONTEXT: 	Debug.error("API", "GLFW no current context!\n\t" + s); return;
			case GLFW_INVALID_ENUM: 		Debug.error("API", "GLFW invalid enum!\n\t" + s); return;
			case GLFW_INVALID_VALUE: 		Debug.error("API", "GLFW invalid value!\n\t" + s); return;
			case GLFW_OUT_OF_MEMORY: 		Debug.error("API", "GLFW out of memory!\n\t" + s); return;
			case GLFW_API_UNAVAILABLE: 		Debug.error("API", "GLFW api unavailable!\n\t" + s); return;
			case GLFW_VERSION_UNAVAILABLE: 	Debug.error("API", "GLFW version unavailable!\n\t" + s); return;
			case GLFW_PLATFORM_ERROR: 		Debug.error("API", "GLFW platform error!\n\t" + s); return;
			case GLFW_FORMAT_UNAVAILABLE: 	Debug.error("API", "GLFW format unavailable!\n\t" + s); return;
			case GLFW_NO_WINDOW_CONTEXT: 	Debug.error("API", "GLFW no window context!\n\t" + s); return;
			}
		});
	}
	
	public static void setKeyCallback(long window, KeyCallback callback) {
		glfwSetKeyCallback(window, 
			(long window_handle, int button, int scancode, int action, int mods) -> {
				switch(action) {
				case GLFW_PRESS: action = PRESS; break;
				case GLFW_REPEAT: action = REPEAT; break;
				case GLFW_RELEASE: action = RELEASE; break;
				default: action = UNKNOWN;
				}
				callback.invoke(button, action, mods);
			}
		);
	}
	
	public static void setCloseCallback(long window, CloseCallback callback) {
		glfwSetWindowCloseCallback(window, callback);
	}
	
	@FunctionalInterface
	public interface ErrorCallback extends GLFWErrorCallbackI {}
	@FunctionalInterface
	public static interface CloseCallback extends GLFWWindowCloseCallbackI {}
	@FunctionalInterface
	public static interface KeyCallback {public void invoke(int button, int action, int mods);}
	
//	@FunctionalInterface
//	public interface ActionCallback {public void invoke();}
//	@FunctionalInterface //TODO: Needs state stuff
//	public interface StateCallback {public void invoke();}
//	@FunctionalInterface
//	public interface RangeCallback {public void invoke(float value);}
}
