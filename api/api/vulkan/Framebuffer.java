package api.vulkan;

import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_FRAMEBUFFER_CREATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_SUCCESS;
import static org.lwjgl.vulkan.VK10.vkCreateFramebuffer;
import static org.lwjgl.vulkan.VK10.vkDestroyFramebuffer;

import java.nio.LongBuffer;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkFramebufferCreateInfo;

public class Framebuffer {
	private VkDevice device;
	long[] framebuffers;
	public Framebuffer(Device device, Swapchain swapchain, Imageview imageview, Renderpass renderpass, long depthBuffer) {
		try(MemoryStack stack = MemoryStack.stackPush()) {
			this.device = device.device;
			LongBuffer lb = stack.mallocLong(1);
			framebuffers = new long[imageview.imageviews.length];
			
			VkFramebufferCreateInfo framebufferinfo = VkFramebufferCreateInfo.callocStack(stack)
			.sType(VK_STRUCTURE_TYPE_FRAMEBUFFER_CREATE_INFO)
			.renderPass(renderpass.renderpass)
			.width(swapchain.extent.width())
			.height(swapchain.extent.height())
			.layers(1);
			
			for(int i = 0; i < imageview.imageviews.length; i++) {
				framebufferinfo.pAttachments(stack.longs(imageview.imageviews[i], depthBuffer));
				
				if(vkCreateFramebuffer(this.device, framebufferinfo, null, lb) != VK_SUCCESS) System.err.println("Failed to create framebuffer " + i);
				framebuffers[i] = lb.get(0);
			}
			
		}
	}
	
	public int length() {return framebuffers.length;}
	public long get(int index) {return framebuffers[index];}
	
	public void dispose() {
		for(int i = 0; i < framebuffers.length; i++) vkDestroyFramebuffer(this.device, framebuffers[i], null);
	}
}
