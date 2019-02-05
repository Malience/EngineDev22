package api.vulkan;

import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_APPLICATION_INFO;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_INSTANCE_CREATE_INFO;

import java.nio.IntBuffer;
import java.util.ArrayList;

import org.lwjgl.PointerBuffer;
import org.lwjgl.glfw.GLFWVulkan;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.EXTDebugUtils;
import org.lwjgl.vulkan.VK;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkApplicationInfo;
import org.lwjgl.vulkan.VkInstance;
import org.lwjgl.vulkan.VkInstanceCreateInfo;
import org.lwjgl.vulkan.VkPhysicalDevice;

import engine.debug.Debug;
import util.Memory;

public class Instance {
	public final VkInstance instance;
	Instance(String applicationName, ArrayList<String> layers, ArrayList<String> extensions) {
		try(MemoryStack stack = MemoryStack.stackPush()) {
			VkApplicationInfo appinfo = VkApplicationInfo.callocStack(stack)
			.sType(VK_STRUCTURE_TYPE_APPLICATION_INFO)
            .pApplicationName(stack.UTF8(applicationName))
            .apiVersion(VK.getInstanceVersionSupported());
			VkInstanceCreateInfo createinfo = VkInstanceCreateInfo.callocStack(stack)
            .sType(VK_STRUCTURE_TYPE_INSTANCE_CREATE_INFO)
            .pApplicationInfo(appinfo);
			
			//Layers
			layers.add("VK_LAYER_LUNARG_standard_validation");
			createinfo.ppEnabledLayerNames(Memory.stackPointers(stack, layers));
			
			//Extensions
			Memory.add(extensions, GLFWVulkan.glfwGetRequiredInstanceExtensions());
			extensions.add(EXTDebugUtils.VK_EXT_DEBUG_UTILS_EXTENSION_NAME);
			createinfo.ppEnabledExtensionNames(Memory.stackPointers(stack, extensions));
			
			PointerBuffer inst = stack.mallocPointer(1);
			int result = VK10.vkCreateInstance(createinfo, null, inst);
			if(result != VK10.VK_SUCCESS) Debug.error("API", "Vulkan instance creation failed!");
			instance = new VkInstance(inst.get(0), createinfo);
		}
	}
	
	public PhysicalDevice[] getPhysicalDevices() {
		try(MemoryStack stack = MemoryStack.stackPush()) {
			IntBuffer ib = stack.mallocInt(1);
			VK10.vkEnumeratePhysicalDevices(instance, ib, null);
			int deviceCount = ib.get(0);
			if(deviceCount <= 0) Debug.error("API", "No supported GPU found!");
			
			PointerBuffer devices = stack.mallocPointer(deviceCount);
			VK10.vkEnumeratePhysicalDevices(instance, ib, devices);
			
			PhysicalDevice[] physicalDevices = new PhysicalDevice[deviceCount];
			for(int i = 0; i < deviceCount; i++) physicalDevices[i] = new PhysicalDevice(new VkPhysicalDevice(devices.get(i), instance));
			return physicalDevices;
		}
	}
	
	public PhysicalDevice getBestPhysicalDevice() {
		PhysicalDevice[] physicalDevices = getPhysicalDevices();
		PhysicalDevice out = null;
		for(int i = 0; i < physicalDevices.length; i++) if(physicalDevices[i].suitable) out = physicalDevices[i];
		if(out == null) Debug.error("API", "No suitable GPU found!");
		return out;
	}
	
	void dispose() {
		VK10.vkDestroyInstance(instance, null);
	}
}
