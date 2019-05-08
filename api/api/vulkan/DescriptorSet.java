package api.vulkan;

import static org.lwjgl.vulkan.VK10.VK_SUCCESS;

import java.nio.LongBuffer;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkDescriptorBufferInfo;
import org.lwjgl.vulkan.VkDescriptorImageInfo;
import org.lwjgl.vulkan.VkDescriptorSetAllocateInfo;
import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkImageSubresourceLayers;
import org.lwjgl.vulkan.VkWriteDescriptorSet;

import engine.debug.Debug;
import engine.rendering.Texture;

public class DescriptorSet {
	private final VkDevice device;
	public final long set;
	
	public DescriptorSet(Device device, DescriptorPool pool, DescriptorLayout layout) {
		this.device = device.device;
		try(MemoryStack stack = MemoryStack.stackPush()) {
			LongBuffer lb = stack.mallocLong(1);
			VkDescriptorSetAllocateInfo setAllocateInfo = VkDescriptorSetAllocateInfo.callocStack(stack)
			.sType(VK10.VK_STRUCTURE_TYPE_DESCRIPTOR_SET_ALLOCATE_INFO)
			.descriptorPool(pool.pool)
			.pSetLayouts(stack.longs(layout.layout));
			
			int result = VK10.vkAllocateDescriptorSets(device.device, setAllocateInfo, lb);
			if(result != VK_SUCCESS) {
				Debug.error("API", "Failed to Allocate Descriptor Sets!");
				System.out.println(result);
			}
			set = lb.get(0);
		}
	}
	
	public void bindBuffer(BufferObject buffer, int binding) {
		try(MemoryStack stack = MemoryStack.stackPush()) {
			VkDescriptorBufferInfo.Buffer desBufferInfo = VkDescriptorBufferInfo.callocStack(1, stack)
			.buffer(buffer.getBuffer())
			.offset(0L)
			.range(VK10.VK_WHOLE_SIZE);
			
			VkWriteDescriptorSet.Buffer desWrite = VkWriteDescriptorSet.callocStack(1, stack)
			.sType(VK10.VK_STRUCTURE_TYPE_WRITE_DESCRIPTOR_SET)
			.dstSet(set)
			.dstBinding(binding)
			.dstArrayElement(0)
			.descriptorType(VK10.VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER)
			.pBufferInfo(desBufferInfo)
			.pImageInfo(null)
			.pTexelBufferView(null);
			VK10.vkUpdateDescriptorSets(device, desWrite, null);
		}
	}
	
	public void bindImageview(long imageview, long sampler, int binding) {
		try(MemoryStack stack = MemoryStack.stackPush()) {
			VkDescriptorImageInfo.Buffer imageInfo = VkDescriptorImageInfo.callocStack(1, stack)
			.imageLayout(VK10.VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL)
			.imageView(imageview)
			.sampler(sampler);
			
			VkWriteDescriptorSet.Buffer desWrite = VkWriteDescriptorSet.callocStack(1, stack)
			.sType(VK10.VK_STRUCTURE_TYPE_WRITE_DESCRIPTOR_SET)
			.dstSet(set)
			.dstBinding(binding)
			.dstArrayElement(0)
			.descriptorType(VK10.VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER)
			.pBufferInfo(null)
			.pImageInfo(imageInfo)
			.pTexelBufferView(null);
			VK10.vkUpdateDescriptorSets(device, desWrite, null);
		}
	}
	
	public void bindTextures(Texture[] textures, int binding) {
		try(MemoryStack stack = MemoryStack.stackPush()) {
			int numTextures = textures.length;
			
			VkDescriptorImageInfo.Buffer imageInfo = VkDescriptorImageInfo.callocStack(numTextures, stack);
			for(int i = 0; i < numTextures; i++) {
				imageInfo.get(i)
				.imageLayout(VK10.VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL)
				.imageView(textures[i].imageView)
				.sampler(0);
			}
			
			VkWriteDescriptorSet.Buffer desWrite = VkWriteDescriptorSet.callocStack(1, stack)
			.sType(VK10.VK_STRUCTURE_TYPE_WRITE_DESCRIPTOR_SET)
			.dstSet(set)
			.dstBinding(binding)
			.dstArrayElement(0)
			.descriptorType(VK10.VK_DESCRIPTOR_TYPE_SAMPLED_IMAGE)
			.pBufferInfo(null)
			.pImageInfo(imageInfo)
			.pTexelBufferView(null);
			VK10.vkUpdateDescriptorSets(device, desWrite, null);
		}
	}
	
	public void bindSampler(long sampler, int binding) {
		try(MemoryStack stack = MemoryStack.stackPush()) {
			VkDescriptorImageInfo.Buffer imageInfo = VkDescriptorImageInfo.callocStack(1, stack)
			.imageLayout(VK10.VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL)
			.imageView(0)
			.sampler(sampler);
			
			VkWriteDescriptorSet.Buffer desWrite = VkWriteDescriptorSet.callocStack(1, stack)
			.sType(VK10.VK_STRUCTURE_TYPE_WRITE_DESCRIPTOR_SET)
			.dstSet(set)
			.dstBinding(binding)
			.dstArrayElement(0)
			.descriptorType(VK10.VK_DESCRIPTOR_TYPE_SAMPLER)
			.pBufferInfo(null)
			.pImageInfo(imageInfo)
			.pTexelBufferView(null);
			VK10.vkUpdateDescriptorSets(device, desWrite, null);
		}
	}
}
