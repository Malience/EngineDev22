package api.vulkan;

import static org.lwjgl.vulkan.VK10.VK_SUCCESS;

import java.nio.LongBuffer;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkDescriptorBufferInfo;
import org.lwjgl.vulkan.VkDescriptorSetAllocateInfo;
import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkWriteDescriptorSet;

import engine.debug.Debug;

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
	
	public void bind(BufferObject buffer, int binding) {
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
}
