package api.vulkan;

import java.util.ArrayList;

import org.lwjgl.glfw.GLFWVulkan;

public abstract class Vulkan {
	private static Instance instance;
	private static Messenger messenger;
	public static boolean supported() {return GLFWVulkan.glfwVulkanSupported();}
	
	public static Instance createInstance() {return createInstance("No Application Name", new ArrayList<>(), new ArrayList<>());}
	public static Instance createInstance(String applicationName, ArrayList<String> layers, ArrayList<String> extensions) {
		if(instance != null) return instance;
		instance = new Instance(applicationName, layers, extensions);
		//try{messenger = new Messenger(instance);}catch(Exception e) {e.printStackTrace();}
		return instance;
	}
	
	public static void dispose() {
		if(instance == null) return;
		if(messenger != null) messenger.dispose(instance);
		instance.dispose();
		instance = null;
	}
}
