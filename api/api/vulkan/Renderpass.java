package api.vulkan;

import org.lwjgl.vulkan.VkAttachmentDescription;
import org.lwjgl.vulkan.VkAttachmentReference;
import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkRenderPassCreateInfo;
import org.lwjgl.vulkan.VkSubpassDependency;
import org.lwjgl.vulkan.VkSubpassDescription;

import engine.debug.Debug;

import static org.lwjgl.vulkan.VK10.*;

import java.nio.LongBuffer;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.KHRSwapchain;
import org.lwjgl.vulkan.VK10;

public class Renderpass {
	private final VkDevice device;
	public final long renderpass;
	
	public Renderpass(Device device, Swapchain swapchain) {
		try(MemoryStack stack = MemoryStack.stackPush()) {
			this.device = device.device;
			VkAttachmentDescription.Buffer attachments = VkAttachmentDescription.callocStack(1, stack)
			.samples(VK_SAMPLE_COUNT_1_BIT)
			.loadOp(VK_ATTACHMENT_LOAD_OP_CLEAR)
			.storeOp(VK_ATTACHMENT_STORE_OP_STORE)
			.stencilLoadOp(VK_ATTACHMENT_LOAD_OP_DONT_CARE)
			.stencilStoreOp(VK_ATTACHMENT_STORE_OP_DONT_CARE)
			.initialLayout(VK_IMAGE_LAYOUT_UNDEFINED)
			.finalLayout(KHRSwapchain.VK_IMAGE_LAYOUT_PRESENT_SRC_KHR)
			.format(swapchain.format);
			
//			attachments.get(1)
//			.samples(VK_SAMPLE_COUNT_1_BIT)
//			.loadOp(VK_ATTACHMENT_LOAD_OP_CLEAR)
//			.storeOp(VK_ATTACHMENT_STORE_OP_DONT_CARE)
//			.stencilLoadOp(VK_ATTACHMENT_LOAD_OP_DONT_CARE)
//			.stencilStoreOp(VK_ATTACHMENT_STORE_OP_DONT_CARE)
//			.initialLayout(VK_IMAGE_LAYOUT_UNDEFINED)
//			.finalLayout(VK10.VK_IMAGE_LAYOUT_DEPTH_STENCIL_ATTACHMENT_OPTIMAL)
//			.format(swapchain.format);
			
			VkAttachmentReference.Buffer ref = VkAttachmentReference.callocStack(1, stack)
			.attachment(0)
			.layout(VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL);
			VkAttachmentReference dref = VkAttachmentReference.callocStack(stack)
			.attachment(1)
			.layout(VK10.VK_IMAGE_LAYOUT_DEPTH_STENCIL_ATTACHMENT_OPTIMAL);
			
			VkSubpassDescription.Buffer subpasses = VkSubpassDescription.callocStack(1, stack)
			.pipelineBindPoint(VK_PIPELINE_BIND_POINT_GRAPHICS)
			.colorAttachmentCount(1)
			.pColorAttachments(ref);
			//.pDepthStencilAttachment(dref);
			
			VkSubpassDependency.Buffer dependency = VkSubpassDependency.callocStack(1, stack)
			.srcSubpass(VK_SUBPASS_EXTERNAL)
			.dstSubpass(0)
			.srcStageMask(VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT)
			.srcAccessMask(0)
			.dstStageMask(VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT)
			.dstAccessMask(VK_ACCESS_COLOR_ATTACHMENT_READ_BIT | VK_ACCESS_COLOR_ATTACHMENT_WRITE_BIT);
			
			VkRenderPassCreateInfo info = VkRenderPassCreateInfo.callocStack(stack)
			.sType(VK_STRUCTURE_TYPE_RENDER_PASS_CREATE_INFO)
			.pAttachments(attachments)
			.pSubpasses(subpasses)
			.pDependencies(dependency);
			
			LongBuffer lb = stack.mallocLong(1);
			if(VK10.vkCreateRenderPass(device.device, info, null, lb) != VK10.VK_SUCCESS)
				Debug.error("API", "Vulkan RenderPass failed to create!");
			else Debug.info("API", "RenderPass created");
			renderpass = lb.get(0);
		}

	}
	
	public void dispose() {
		VK10.vkDestroyRenderPass(this.device, renderpass, null);
	}
	
}
