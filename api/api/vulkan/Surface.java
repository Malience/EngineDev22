package api.vulkan;

import java.nio.IntBuffer;
import java.nio.LongBuffer;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWVulkan;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.KHRSurface;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkExtent2D;
import org.lwjgl.vulkan.VkInstance;
import org.lwjgl.vulkan.VkPhysicalDevice;
import org.lwjgl.vulkan.VkSurfaceCapabilitiesKHR;
import org.lwjgl.vulkan.VkSurfaceFormatKHR;

import engine.window.Window;

public class Surface {
	private final VkInstance instance;
	private final VkPhysicalDevice physicalDevice;
	private final int queueFamily;
	private final long window;
	
	public final long surface;
	public final VkSurfaceFormatKHR.Buffer formats;
	public VkSurfaceCapabilitiesKHR capabilities;
	
	public Surface(Instance instance, PhysicalDevice physicalDevice, QueueFamily queueFamily, Window window) {
		try(MemoryStack stack = MemoryStack.stackPush()) {
			this.instance = instance.instance;
			this.physicalDevice = physicalDevice.device;
			this.queueFamily = queueFamily.index;
			this.window = window.getHandle();
			
			LongBuffer lb = stack.mallocLong(1);
			IntBuffer ib = stack.mallocInt(1);
			
			GLFWVulkan.glfwCreateWindowSurface(this.instance, this.window, null, lb);
			surface = lb.get(0);
			
			KHRSurface.vkGetPhysicalDeviceSurfaceFormatsKHR(this.physicalDevice, surface, ib, null);
			KHRSurface.vkGetPhysicalDeviceSurfaceFormatsKHR(this.physicalDevice, surface, ib, (formats = VkSurfaceFormatKHR.malloc(ib.get(0))));
			
			//TODO: Actually check for support
			KHRSurface.vkGetPhysicalDeviceSurfaceSupportKHR(this.physicalDevice, this.queueFamily, surface, ib);
			
			KHRSurface.vkGetPhysicalDeviceSurfaceCapabilitiesKHR(this.physicalDevice, surface, (capabilities = VkSurfaceCapabilitiesKHR.malloc()));
		}
	}
	
	public VkExtent2D getExtent() {
		try(MemoryStack stack = MemoryStack.stackPush()) {
			KHRSurface.vkGetPhysicalDeviceSurfaceCapabilitiesKHR(this.physicalDevice, surface, (capabilities = VkSurfaceCapabilitiesKHR.malloc()));
			if(capabilities.currentExtent().width() != Integer.MAX_VALUE) {
				return capabilities.currentExtent();
			}
			IntBuffer w = stack.mallocInt(1);
			IntBuffer h = stack.mallocInt(1);
			//GLFW.glfwGetFramebufferSize(window.getHandle(), w, h);
			return VkExtent2D.malloc().set(w.get(0), h.get(0));
		}
	}
	
	public void dispose() {
		KHRSurface.vkDestroySurfaceKHR(this.instance, surface, null);
	}
}
