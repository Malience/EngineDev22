package api.vulkan;

import static org.lwjgl.vulkan.VK10.*;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkBufferCopy;
import org.lwjgl.vulkan.VkBufferImageCopy;
import org.lwjgl.vulkan.VkClearValue;
import org.lwjgl.vulkan.VkCommandBuffer;
import org.lwjgl.vulkan.VkCommandBufferBeginInfo;
import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkImageMemoryBarrier;
import org.lwjgl.vulkan.VkRenderPassBeginInfo;

import engine.debug.Debug;

public class CommandBuffer extends VkCommandBuffer {
	CommandBuffer(long handle, VkDevice device) {super(handle, device);}
	
	public void begin() {
		try(MemoryStack stack = MemoryStack.stackPush()) {
			VkCommandBufferBeginInfo begininfo = VkCommandBufferBeginInfo.callocStack(stack)
			.sType(VK_STRUCTURE_TYPE_COMMAND_BUFFER_BEGIN_INFO)
			.flags(VK_COMMAND_BUFFER_USAGE_SIMULTANEOUS_USE_BIT)
			.pInheritanceInfo(null);
			if(vkBeginCommandBuffer(this, begininfo) != VK_SUCCESS)
				Debug.error("API", "Command Buffer " + this.address() + ": didn't begin!");
		}
	}
	public void beginOnce() {
		try(MemoryStack stack = MemoryStack.stackPush()) {
			VkCommandBufferBeginInfo begininfo = VkCommandBufferBeginInfo.callocStack(stack)
			.sType(VK_STRUCTURE_TYPE_COMMAND_BUFFER_BEGIN_INFO)
			.flags(VK_COMMAND_BUFFER_USAGE_ONE_TIME_SUBMIT_BIT)
			.pInheritanceInfo(null);
			if(vkBeginCommandBuffer(this, begininfo) != VK_SUCCESS)
				Debug.error("API", "Command Buffer " + this.address() + ": didn't begin!");
		}
	}
	public void end() {vkEndCommandBuffer(this);}
	
	public void beginRenderPass(Renderpass renderpass, long framebuffer, int width, int height) {
		beginRenderPass(renderpass.renderpass, framebuffer, width, height);
	}
	public void beginRenderPass(long renderpass, long framebuffer, int width, int height) {
		try(MemoryStack stack = MemoryStack.stackPush()) {
			VkRenderPassBeginInfo renderbegininfo = VkRenderPassBeginInfo.callocStack(stack)
			.sType(VK_STRUCTURE_TYPE_RENDER_PASS_BEGIN_INFO)
			.renderPass(renderpass)
			.framebuffer(framebuffer);
			renderbegininfo.renderArea().offset().set(0, 0);
			renderbegininfo.renderArea().extent().set(width,  height);
			VkClearValue.Buffer clearvalue = VkClearValue.callocStack(2, stack);
			clearvalue.get(0).color().float32(3, 1);
			clearvalue.get(1).depthStencil().depth(1).stencil(0);
			renderbegininfo.pClearValues(clearvalue);
			
			
			vkCmdBeginRenderPass(this, renderbegininfo, VK_SUBPASS_CONTENTS_INLINE);
		}
	}
	public void endRenderPass() {vkCmdEndRenderPass(this);}
	
	public void bindPipelineGraphics(GraphicsPipeline pipeline) {vkCmdBindPipeline(this, VK_PIPELINE_BIND_POINT_GRAPHICS, pipeline.pipeline);}
	public void bindPipelineGraphics(long pipeline) {vkCmdBindPipeline(this, VK_PIPELINE_BIND_POINT_GRAPHICS, pipeline);}
	public void bindPipelineCompute(GraphicsPipeline pipeline) {vkCmdBindPipeline(this, VK_PIPELINE_BIND_POINT_COMPUTE, pipeline.pipeline);}
	
	public void bindVertexBuffer(BufferObject buffer) {
		try(MemoryStack stack = MemoryStack.stackPush()) {
			VK10.vkCmdBindVertexBuffers(this, 0, stack.longs(buffer.getBuffer()), stack.callocLong(1));
		}
	}
	
	public void bindIndexBuffer(BufferObject ib) {
		VK10.vkCmdBindIndexBuffer(this, ib.getBuffer(), 0L, VK10.VK_INDEX_TYPE_UINT32);
	}
	
	public void bindUniforms(long pipelineLayout, long descriptorSet) {
		try(MemoryStack stack = MemoryStack.stackPush()) {
			vkCmdBindDescriptorSets(this, VK_PIPELINE_BIND_POINT_GRAPHICS, pipelineLayout, 0, stack.longs(descriptorSet), null);
		}
	}
	
	public void bindUniforms(long pipelineLayout, LongBuffer descriptorSets) {
		vkCmdBindDescriptorSets(this, VK_PIPELINE_BIND_POINT_GRAPHICS, pipelineLayout, 0, descriptorSets, null);
	}
	
	public void draw(int vertexCount, int instanceCount, int firstVertex, int firstInstance) 
	{vkCmdDraw(this, vertexCount, instanceCount, firstVertex, firstInstance);}
	
	public void draw(int indexCount, int instanceCount) {vkCmdDrawIndexed(this, indexCount, instanceCount, 0, 0, 0);}
	
	public void copyBuffer(long src, long dst, int size) {
		try(MemoryStack stack = MemoryStack.stackPush()) {
			VkBufferCopy.Buffer copy = VkBufferCopy.callocStack(1, stack).size(size);
			vkCmdCopyBuffer(this, src, dst, copy);
		}
	}
	
	
	public void pushConstants(long layout, int stageFlags, int offset, IntBuffer values)
	{vkCmdPushConstants(this, layout, stageFlags, offset, values);}
	public void pushConstants(long layout, int stageFlags, int offset, FloatBuffer values)
	{vkCmdPushConstants(this, layout, stageFlags, offset, values);}
	public void pushConstants(long layout, int stageFlags, int offset, ByteBuffer values)
	{vkCmdPushConstants(this, layout, stageFlags, offset, values);}
	public void pushConstants(long layout, int stageFlags, int offset, int size, long values)
	{nvkCmdPushConstants(this, layout, stageFlags, offset, size, values);}
	
	int getAccessMask(int layout) {
		switch(layout) {
		case VK_IMAGE_LAYOUT_UNDEFINED: return 0;
		case VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL: return VK_ACCESS_TRANSFER_WRITE_BIT;
		case VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL: return VK_ACCESS_SHADER_READ_BIT;
		}
		return -1;
	}
	
	void copyBuffer(VkCommandBuffer commandBuffer, long src, long dst, long size) {
		try(MemoryStack stack = MemoryStack.stackPush()) {
			VkBufferCopy.Buffer copyRegion = VkBufferCopy.callocStack(1, stack)
			.size(size);
			vkCmdCopyBuffer(commandBuffer, src, dst, copyRegion);
		}
	}
	
	public void transitionImageLayout(long image, int format, int oldLayout, int newLayout, int srcStage, int dstStage) {
		try(MemoryStack stack = MemoryStack.stackPush()) {
			VkImageMemoryBarrier.Buffer barrier = VkImageMemoryBarrier.callocStack(1, stack)
			.sType(VK_STRUCTURE_TYPE_IMAGE_MEMORY_BARRIER)
			.oldLayout(oldLayout)
			.newLayout(newLayout)
			.srcQueueFamilyIndex(VK_QUEUE_FAMILY_IGNORED)
			.dstQueueFamilyIndex(VK_QUEUE_FAMILY_IGNORED)
			.image(image)
			.srcAccessMask(getAccessMask(oldLayout))
			.dstAccessMask(getAccessMask(newLayout));
			barrier.subresourceRange()
			.aspectMask(VK_IMAGE_ASPECT_COLOR_BIT)
			.baseMipLevel(0)
			.levelCount(1)
			.baseArrayLayer(0)
			.layerCount(1);
			
			vkCmdPipelineBarrier(
					this, 
					srcStage, dstStage, 
					0, 
					null, 
					null, 
					barrier);
		}
	}
	
	public void copyBufferToImage(long buffer, long image, int width, int height) {
		try(MemoryStack stack = MemoryStack.stackPush()) {
			VkBufferImageCopy.Buffer region = VkBufferImageCopy.callocStack(1, stack)
			.bufferOffset(0)
			.bufferRowLength(0)
			.bufferImageHeight(0);
			
			region.imageSubresource()
			.aspectMask(VK_IMAGE_ASPECT_COLOR_BIT)
			.mipLevel(0)
			.baseArrayLayer(0)
			.layerCount(1);
			
			region.imageOffset().set(0, 0, 0);
			region.imageExtent().set(width, height, 1);
			
			vkCmdCopyBufferToImage(this, buffer, image, VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL, region);
		}
	}
}
