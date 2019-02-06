package api.vulkan;

import java.nio.LongBuffer;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkDescriptorPoolCreateInfo;
import org.lwjgl.vulkan.VkDescriptorPoolSize;
import org.lwjgl.vulkan.VkDevice;

public class DescriptorPool {
	private final VkDevice device;
	public final long pool;
	
	public DescriptorPool(Device device, int numSets) {
		this.device = device.device;
		try(MemoryStack stack = MemoryStack.stackPush()) {
			LongBuffer lb = stack.mallocLong(1);
			VkDescriptorPoolSize.Buffer poolSize = VkDescriptorPoolSize.callocStack(1, stack)
			.type(VK10.VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER)
			.descriptorCount(1);
			
			VkDescriptorPoolCreateInfo poolInfo = VkDescriptorPoolCreateInfo.callocStack(stack)
			.sType(VK10.VK_OBJECT_TYPE_DESCRIPTOR_POOL)
			.pPoolSizes(poolSize)
			.maxSets(numSets);
			
			VK10.vkCreateDescriptorPool(this.device, poolInfo, null, lb);
			pool = lb.get(0);
		}
	}
	
	public void dispose() {VK10.vkDestroyCommandPool(device, pool, null);}
}
