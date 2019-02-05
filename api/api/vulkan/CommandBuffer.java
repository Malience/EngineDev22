package api.vulkan;

import static org.lwjgl.vulkan.VK10.*;

import java.nio.LongBuffer;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkBufferCopy;
import org.lwjgl.vulkan.VkClearValue;
import org.lwjgl.vulkan.VkCommandBuffer;
import org.lwjgl.vulkan.VkCommandBufferBeginInfo;
import org.lwjgl.vulkan.VkDevice;
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
		try(MemoryStack stack = MemoryStack.stackPush()) {
			VkRenderPassBeginInfo renderbegininfo = VkRenderPassBeginInfo.callocStack(stack)
			.sType(VK_STRUCTURE_TYPE_RENDER_PASS_BEGIN_INFO)
			.renderPass(renderpass.renderpass)
			.framebuffer(framebuffer);
			renderbegininfo.renderArea().offset().set(0, 0);
			renderbegininfo.renderArea().extent().set(width,  height);
			VkClearValue.Buffer clearvalue = VkClearValue.callocStack(1, stack);
			renderbegininfo.pClearValues(clearvalue);
			
			
			vkCmdBeginRenderPass(this, renderbegininfo, VK_SUBPASS_CONTENTS_INLINE);
		}
	}
	public void endRenderPass() {vkCmdEndRenderPass(this);}
	
	public void bindPipelineGraphics(Pipeline pipeline) {vkCmdBindPipeline(this, VK_PIPELINE_BIND_POINT_GRAPHICS, pipeline.pipeline);}
	public void bindPipelineCompute(Pipeline pipeline) {vkCmdBindPipeline(this, VK_PIPELINE_BIND_POINT_COMPUTE, pipeline.pipeline);}
	
	public void bindVertexBuffer(BufferObject buffer) {
		try(MemoryStack stack = MemoryStack.stackPush()) {
			VK10.vkCmdBindVertexBuffers(this, 0, stack.longs(buffer.getBuffer()), stack.callocLong(1));
		}
	}
	
	public void bindIndexBuffer(BufferObject ib) {
		VK10.vkCmdBindIndexBuffer(this, ib.getBuffer(), 0L, VK10.VK_INDEX_TYPE_UINT32);
	}
	
	public void updateUniforms(long pipelineLayout, long descriptorSet) {
		try(MemoryStack stack = MemoryStack.stackPush()) {
			vkCmdBindDescriptorSets(this, VK_PIPELINE_BIND_POINT_GRAPHICS, pipelineLayout, 0, stack.longs(descriptorSet), null);
		}
	}
	
	public void draw(int vertexCount, int instanceCount, int firstVertex, int firstInstance) 
	{vkCmdDraw(this, vertexCount, instanceCount, firstVertex, firstInstance);}
	
	public void draw(int indices) {vkCmdDrawIndexed(this, indices, 1, 0, 0, 0);}
	
	public void copyBuffer(long src, long dst, int size) {
		try(MemoryStack stack = MemoryStack.stackPush()) {
			VkBufferCopy.Buffer copy = VkBufferCopy.callocStack(1, stack).size(size);
			vkCmdCopyBuffer(this, src, dst, copy);
		}
	}
}
