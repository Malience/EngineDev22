package api.vulkan;

import java.nio.IntBuffer;
import java.nio.LongBuffer;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.KHRSurface;
import org.lwjgl.vulkan.KHRSwapchain;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkExtent2D;
import org.lwjgl.vulkan.VkPhysicalDevice;
import org.lwjgl.vulkan.VkSwapchainCreateInfoKHR;

public class Swapchain {
	private final VkDevice device;
	
	public final long swapchain;
	public final VkExtent2D extent;
	public final int format;
	
	public Swapchain(Device device, Surface surface) {
		try(MemoryStack stack = MemoryStack.stackPush()) {
			this.device = device.device;
			LongBuffer lb = stack.mallocLong(1);
			
			VkSwapchainCreateInfoKHR swapInfo = VkSwapchainCreateInfoKHR.callocStack(stack)
			.sType(KHRSwapchain.VK_STRUCTURE_TYPE_SWAPCHAIN_CREATE_INFO_KHR)
			.surface(surface.surface)
			.minImageCount(surface.capabilities.minImageCount())
			.imageFormat((format = VK10.VK_FORMAT_B8G8R8A8_UNORM))
			.imageColorSpace(surface.formats.get(0).colorSpace())
			.preTransform(KHRSurface.VK_SURFACE_TRANSFORM_IDENTITY_BIT_KHR)
			.imageExtent((this.extent = VkExtent2D.malloc().set(surface.getExtent())))
			.imageArrayLayers(1)
			.imageUsage(VK10.VK_IMAGE_USAGE_COLOR_ATTACHMENT_BIT)
			.imageSharingMode(VK10.VK_SHARING_MODE_EXCLUSIVE)
			.pQueueFamilyIndices(null)
			//.presentMode(KHRSurface.VK_PRESENT_MODE_FIFO_KHR)
			.presentMode(KHRSurface.VK_PRESENT_MODE_IMMEDIATE_KHR)
			.oldSwapchain(0)
			.clipped(true)
			.compositeAlpha(KHRSurface.VK_COMPOSITE_ALPHA_OPAQUE_BIT_KHR);
			
			if(KHRSwapchain.vkCreateSwapchainKHR(this.device, swapInfo, null, lb) != VK10.VK_SUCCESS)
				System.out.println("Swapchain creation failed");
			swapchain = lb.get(0);
		}
	}
	
	public Swapchain(Device device, Surface surface, IntBuffer width, IntBuffer height) {
		try(MemoryStack stack = MemoryStack.stackPush()) {
			this.device = device.device;
			LongBuffer lb = stack.mallocLong(1);
			
			VkSwapchainCreateInfoKHR swapInfo = VkSwapchainCreateInfoKHR.callocStack(stack)
			.sType(KHRSwapchain.VK_STRUCTURE_TYPE_SWAPCHAIN_CREATE_INFO_KHR)
			.surface(surface.surface)
			.minImageCount(surface.capabilities.minImageCount())
			.imageFormat((format = VK10.VK_FORMAT_B8G8R8A8_UNORM))
			.imageColorSpace(surface.formats.get(0).colorSpace())
			.preTransform(KHRSurface.VK_SURFACE_TRANSFORM_IDENTITY_BIT_KHR)
			.imageExtent((this.extent = VkExtent2D.malloc().set(width.get(0), height.get(0))))
			.imageArrayLayers(1)
			.imageUsage(VK10.VK_IMAGE_USAGE_COLOR_ATTACHMENT_BIT)
			.imageSharingMode(VK10.VK_SHARING_MODE_EXCLUSIVE)
			.pQueueFamilyIndices(null)
			.presentMode(KHRSurface.VK_PRESENT_MODE_MAILBOX_KHR)//.VK_PRESENT_MODE_FIFO_KHR)
			.oldSwapchain(0)
			.clipped(true)
			.compositeAlpha(KHRSurface.VK_COMPOSITE_ALPHA_OPAQUE_BIT_KHR);
			
			if(KHRSwapchain.vkCreateSwapchainKHR(this.device, swapInfo, null, lb) != VK10.VK_SUCCESS)
				System.out.println("Swapchain creation failed");
			swapchain = lb.get(0);
		}
	}
	
	public void dispose() {
		try {
			extent.free();
		} catch(Exception e) {
			e.printStackTrace();
		}
		KHRSwapchain.vkDestroySwapchainKHR(this.device, swapchain, null);
	}
}
