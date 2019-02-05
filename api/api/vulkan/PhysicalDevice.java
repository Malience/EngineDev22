package api.vulkan;

import java.nio.IntBuffer;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkPhysicalDevice;
import org.lwjgl.vulkan.VkPhysicalDeviceFeatures;
import org.lwjgl.vulkan.VkPhysicalDeviceProperties;
import org.lwjgl.vulkan.VkQueueFamilyProperties;

public class PhysicalDevice {
	public final VkPhysicalDevice device;
	public final VkPhysicalDeviceProperties properties;
	public final VkPhysicalDeviceFeatures features;
	
	public final boolean suitable;
	
	public PhysicalDevice(VkPhysicalDevice device) {
		this.device = device;
		
		properties = VkPhysicalDeviceProperties.malloc();
		features = VkPhysicalDeviceFeatures.malloc();
		
		VK10.vkGetPhysicalDeviceProperties(device, properties);
		VK10.vkGetPhysicalDeviceFeatures(device, features);
		
		suitable = properties.deviceType() == VK10.VK_PHYSICAL_DEVICE_TYPE_DISCRETE_GPU && features.geometryShader();
	}
	
	public QueueFamily[] getQueueFamilies() {
		try(MemoryStack stack = MemoryStack.stackPush()) {
			IntBuffer ib = stack.mallocInt(1);
			VK10.vkGetPhysicalDeviceQueueFamilyProperties(device, ib, null);
			
			VkQueueFamilyProperties.Buffer properties = VkQueueFamilyProperties.malloc(ib.get(0));
			VK10.vkGetPhysicalDeviceQueueFamilyProperties(device, ib, properties);
			
			int num = properties.limit();
			QueueFamily[] families = new QueueFamily[num];
			for(int i = 0; i < num; i++) families[i] = new QueueFamily(i, properties.get(i));
			return families;
		}
	}
}
