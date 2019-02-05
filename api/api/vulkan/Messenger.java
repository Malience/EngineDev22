package api.vulkan;

import java.nio.LongBuffer;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.EXTDebugUtils;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkDebugUtilsMessengerCallbackDataEXT;
import org.lwjgl.vulkan.VkDebugUtilsMessengerCallbackEXT;
import org.lwjgl.vulkan.VkDebugUtilsMessengerCallbackEXTI;
import org.lwjgl.vulkan.VkDebugUtilsMessengerCreateInfoEXT;

public class Messenger {
	public final long messenger;
	
	public Messenger(Instance instance) {
		this(instance,
		//Severity
		EXTDebugUtils.VK_DEBUG_UTILS_MESSAGE_SEVERITY_VERBOSE_BIT_EXT 
		| EXTDebugUtils.VK_DEBUG_UTILS_MESSAGE_SEVERITY_WARNING_BIT_EXT 
		| EXTDebugUtils.VK_DEBUG_UTILS_MESSAGE_SEVERITY_ERROR_BIT_EXT,
		//Type
		EXTDebugUtils.VK_DEBUG_UTILS_MESSAGE_TYPE_GENERAL_BIT_EXT 
		| EXTDebugUtils.VK_DEBUG_UTILS_MESSAGE_TYPE_VALIDATION_BIT_EXT 
		| EXTDebugUtils.VK_DEBUG_UTILS_MESSAGE_TYPE_PERFORMANCE_BIT_EXT,
		//Callback
		(int messageSeverity, int messageType, long pCallbackData, long pUserData) -> {
			VkDebugUtilsMessengerCallbackDataEXT callbackData = VkDebugUtilsMessengerCallbackDataEXT.create(pCallbackData);
			
			System.err.println("validation layer: " + callbackData.pMessageString());
			return VK10.VK_FALSE;
		});
	}
	
	public Messenger(Instance instance, int severity, int type, VulkanDebugCallback callback) {
		try(MemoryStack stack = MemoryStack.stackPush()) {
			VkDebugUtilsMessengerCallbackEXT pCallback = VkDebugUtilsMessengerCallbackEXT.create(callback);
			VkDebugUtilsMessengerCreateInfoEXT createInfo = VkDebugUtilsMessengerCreateInfoEXT.callocStack(stack)
			.sType(EXTDebugUtils.VK_STRUCTURE_TYPE_DEBUG_UTILS_MESSENGER_CREATE_INFO_EXT)
			.messageSeverity(severity)
			.messageType(type)
			.pfnUserCallback(pCallback);
			//VK10.vkGetInstanceProcAddr(instance, "vkCreateDebugUtilsMessengerEXT");
			LongBuffer lb = stack.mallocLong(1);
			if(instance == null) System.out.println("Instance doesn't exist");
			if(EXTDebugUtils.vkCreateDebugUtilsMessengerEXT(instance.instance, createInfo, null, lb) != VK10.VK_SUCCESS) System.err.println("Could not create debug messenger!");
			messenger = lb.get(0);
		}
	}
	
	public void dispose(Instance instance) {
		EXTDebugUtils.vkDestroyDebugUtilsMessengerEXT(instance.instance, messenger, null);
	}
	
	public static interface VulkanDebugCallback extends VkDebugUtilsMessengerCallbackEXTI {}
}
