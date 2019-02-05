package api.vulkan;

import static org.lwjgl.vulkan.VK10.vkCreateImageView;

import java.nio.IntBuffer;
import java.nio.LongBuffer;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.KHRSwapchain;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkImageViewCreateInfo;

import static org.lwjgl.vulkan.VK10.VK_COMPONENT_SWIZZLE_IDENTITY;

public class Imageview {
	private final VkDevice device;
	public final long[] imageviews;
	public Imageview(Device device, Swapchain swapchain) {
		try(MemoryStack stack = MemoryStack.stackPush()) {
			this.device = device.device;
			IntBuffer ib = stack.mallocInt(1);
			LongBuffer lb = stack.mallocLong(1);
			
			KHRSwapchain.vkGetSwapchainImagesKHR(this.device, swapchain.swapchain, ib, null);
			LongBuffer images = stack.mallocLong(ib.get(0));
			KHRSwapchain.vkGetSwapchainImagesKHR(this.device, swapchain.swapchain, ib, images);
			
			VkImageViewCreateInfo viewInfo = VkImageViewCreateInfo.callocStack(stack)
			.sType(VK10.VK_STRUCTURE_TYPE_IMAGE_VIEW_CREATE_INFO)
			.viewType(VK10.VK_IMAGE_VIEW_TYPE_2D)
			.format(swapchain.format);
			
			viewInfo.components().set(
					VK_COMPONENT_SWIZZLE_IDENTITY, VK_COMPONENT_SWIZZLE_IDENTITY, 
					VK_COMPONENT_SWIZZLE_IDENTITY, VK_COMPONENT_SWIZZLE_IDENTITY);
			
			viewInfo.subresourceRange()
			.aspectMask(VK10.VK_IMAGE_ASPECT_COLOR_BIT)
			.baseMipLevel(0)
			.levelCount(1)
			.baseArrayLayer(0)
			.layerCount(1);
			
			int max = images.limit();
			imageviews = new long[max];
			for(int i = 0; i < max; i++) {
				viewInfo.image(images.get(i));
				
				vkCreateImageView(this.device, viewInfo, null, lb);
				imageviews[i] = lb.get(0);
			}
		}
	}
	
	public void dispose() {
		for(int i = 0; i < imageviews.length; i++) {
			VK10.vkDestroyImageView(this.device, imageviews[i], null);
		}
	}
}
