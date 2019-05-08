package api.vulkan;

import static org.lwjgl.vulkan.VK10.VK_COMMAND_BUFFER_LEVEL_PRIMARY;
import static org.lwjgl.vulkan.VK10.VK_COMMAND_BUFFER_USAGE_ONE_TIME_SUBMIT_BIT;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_COMMAND_BUFFER_ALLOCATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_COMMAND_BUFFER_BEGIN_INFO;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_COMMAND_POOL_CREATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_SUCCESS;
import static org.lwjgl.vulkan.VK10.vkAllocateCommandBuffers;
import static org.lwjgl.vulkan.VK10.vkBeginCommandBuffer;
import static org.lwjgl.vulkan.VK10.vkCreateCommandPool;
import static org.lwjgl.vulkan.VK10.vkDestroyCommandPool;

import java.nio.LongBuffer;

import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkCommandBuffer;
import org.lwjgl.vulkan.VkCommandBufferAllocateInfo;
import org.lwjgl.vulkan.VkCommandBufferBeginInfo;
import org.lwjgl.vulkan.VkCommandPoolCreateInfo;
import org.lwjgl.vulkan.VkDevice;

import engine.debug.Debug;

public class CommandPool {
	private final VkDevice device;
	public final long commandPool;
	
	public CommandPool(QueueFamily queueFamily, Device device) {this(queueFamily, device, false);}
	public CommandPool(QueueFamily queueFamily, Device device, boolean flag) {
		try(MemoryStack stack = MemoryStack.stackPush()) {
			this.device = device.device;
			LongBuffer lb = stack.mallocLong(1);
			
			VkCommandPoolCreateInfo poolinfo = VkCommandPoolCreateInfo.callocStack(stack)
			.sType(VK_STRUCTURE_TYPE_COMMAND_POOL_CREATE_INFO)
			.queueFamilyIndex(queueFamily.index);
			if(flag) poolinfo.flags(VK10.VK_COMMAND_POOL_CREATE_TRANSIENT_BIT);
			
			if(vkCreateCommandPool(this.device, poolinfo, null, lb) != VK_SUCCESS) 
				Debug.error("API", "Failed to create Command Pool!");
			commandPool = lb.get(0);
		}
	}
	
	public CommandBuffer createBuffer() {
		//TODO: Support Secondary Buffers
		try(MemoryStack stack = MemoryStack.stackPush()) {
			VkCommandBufferAllocateInfo allocateinfo = VkCommandBufferAllocateInfo.callocStack(stack)
			.sType(VK_STRUCTURE_TYPE_COMMAND_BUFFER_ALLOCATE_INFO)
			.commandPool(commandPool)
			.level(VK_COMMAND_BUFFER_LEVEL_PRIMARY)
			.commandBufferCount(1);
			PointerBuffer pCommandBuffer = stack.mallocPointer(1);
			if(vkAllocateCommandBuffers(this.device, allocateinfo, pCommandBuffer) != VK_SUCCESS)
				Debug.error("API", "Failed to allocate Command Buffer!");
			return new CommandBuffer(pCommandBuffer.get(0), this.device);
		}
	}
	
	public CommandBuffer[] createBuffer(int num) {
		try(MemoryStack stack = MemoryStack.stackPush()) {
			VkCommandBufferAllocateInfo allocateinfo = VkCommandBufferAllocateInfo.callocStack(stack)
			.sType(VK_STRUCTURE_TYPE_COMMAND_BUFFER_ALLOCATE_INFO)
			.commandPool(commandPool)
			.level(VK_COMMAND_BUFFER_LEVEL_PRIMARY)
			.commandBufferCount(num);
			PointerBuffer pCommandBuffer = stack.mallocPointer(num);
			if(vkAllocateCommandBuffers(this.device, allocateinfo, pCommandBuffer) != VK_SUCCESS)
				Debug.error("API", "Failed to allocate Command Buffer!");
			CommandBuffer[] buffer = new CommandBuffer[num];
			for(int i = 0; i < num; i++) buffer[i] = new CommandBuffer(pCommandBuffer.get(i), this.device);
			return buffer;
		}
	}
	
	public void destroyBuffer(CommandBuffer buffer) {
		try(MemoryStack stack = MemoryStack.stackPush()) {
			VK10.vkFreeCommandBuffers(this.device, commandPool, stack.pointers(buffer));
		}
	}
	
	public void destroyBuffers(CommandBuffer[] buffers) {
		try(MemoryStack stack = MemoryStack.stackPush()) {
			VK10.vkFreeCommandBuffers(this.device, commandPool, stack.pointers(buffers));
		}
	}
	
	public void dispose() {
		vkDestroyCommandPool(this.device, commandPool, null);
	}
}
