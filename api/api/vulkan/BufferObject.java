package api.vulkan;

import static org.lwjgl.vulkan.VK10.*;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;

import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.vulkan.VkBufferCreateInfo;
import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkMemoryAllocateInfo;
import org.lwjgl.vulkan.VkMemoryRequirements;
import org.lwjgl.vulkan.VkPhysicalDevice;
import org.lwjgl.vulkan.VkPhysicalDeviceMemoryProperties;

import engine.debug.Debug;

public class BufferObject {
	private final VkPhysicalDevice physicalDevice;
	private final VkDevice device;
	private long buffer, memory;
	
	public BufferObject(PhysicalDevice physicalDevice, Device device) {this.physicalDevice = physicalDevice.device; this.device = device.device;}
	
	public long getBuffer() {return buffer;}
	public long getMemory() {return memory;}
	
	public void createVertexBuffer(int size) {
		try(MemoryStack stack = MemoryStack.stackPush()) {
			buffer = createBuffer(size, VK_BUFFER_USAGE_TRANSFER_DST_BIT | VK_BUFFER_USAGE_VERTEX_BUFFER_BIT);
			memory = allocateMemory(buffer, VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT);
		}
	}
	
	public void createIndexBuffer(int size) {
		try(MemoryStack stack = MemoryStack.stackPush()) {
			buffer = createBuffer(size, VK_BUFFER_USAGE_TRANSFER_DST_BIT | VK_BUFFER_USAGE_INDEX_BUFFER_BIT);
			memory = allocateMemory(buffer, VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT);
		}
	}
	
	public void createUniformBuffer(int size) {
		try(MemoryStack stack = MemoryStack.stackPush()) {
			buffer = createBuffer(size, VK_BUFFER_USAGE_UNIFORM_BUFFER_BIT);
			memory = allocateMemory(buffer, VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT | VK_MEMORY_PROPERTY_HOST_COHERENT_BIT);
		}
	}
	
	private void load(Queue queue, CommandPool pool, long data, int size) {
		long stagingBuffer = createBuffer(size, VK_BUFFER_USAGE_TRANSFER_SRC_BIT);
		long stagingMemory = allocateMemory(stagingBuffer, VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT | VK_MEMORY_PROPERTY_HOST_COHERENT_BIT);
		MemoryUtil.memCopy(data, map(size, stagingMemory), size);
		unmap(stagingMemory);
		CommandBuffer vtb = pool.createBuffer(); //Vertex Transfer Buffer
		vtb.beginOnce();
		vtb.copyBuffer(stagingBuffer, buffer, size);
		vtb.end();
		queue.submit(vtb);
		queue.waitIdle();
		pool.destroyBuffer(vtb);
		vkDestroyBuffer(device, stagingBuffer, null);
		vkFreeMemory(device, stagingMemory, null);
	}
	
	public void load(Queue queue, CommandPool pool, Vertex[] vertices) {
		try(MemoryStack stack = MemoryStack.stackPush()) {
			int size = vertices.length * Vertex.SIZEOF;
			FloatBuffer fb = stack.mallocFloat(size>>2);
			for(Vertex v : vertices) {
				fb.put(v.pos.x()); fb.put(v.pos.y()); fb.put(v.pos.z());
				fb.put(v.color.x()); fb.put(v.color.y()); fb.put(v.color.z());
			}
			fb.flip();
			load(queue, pool, MemoryUtil.memAddress(fb), size);
		}
	}
	
	public void load(Queue queue, CommandPool pool, int[] indices) {
		try(MemoryStack stack = MemoryStack.stackPush()) {
			load(queue, pool, MemoryUtil.memAddress(stack.ints(indices)), indices.length * 4);
		}
	}
	
	public void load(Queue queue, CommandPool pool, IntBuffer indices) {
		load(queue, pool, MemoryUtil.memAddress(indices), indices.limit() * 4);
	}
	
	private long createBuffer(int size, int usage) {
		try(MemoryStack stack = MemoryStack.stackPush()) {
			//Create Buffer
			VkBufferCreateInfo bufferInfo = VkBufferCreateInfo.callocStack(stack)
			.sType(VK_STRUCTURE_TYPE_BUFFER_CREATE_INFO)
			.size(size)
			.usage(usage)
			.sharingMode(VK_SHARING_MODE_EXCLUSIVE);
			
			LongBuffer lb = stack.mallocLong(1);
			vkCreateBuffer(device, bufferInfo, null, lb);
			return lb.get(0);
		}
	}
	
	private long allocateMemory(long buffer, int prop) {
		try(MemoryStack stack = MemoryStack.stackPush()) {
			//Memory Requirements
			VkMemoryRequirements req = VkMemoryRequirements.mallocStack(stack);
			vkGetBufferMemoryRequirements(device, buffer, req);
			//Prop and Allocate
			VkPhysicalDeviceMemoryProperties pdeviceprop = VkPhysicalDeviceMemoryProperties.mallocStack(stack);
			vkGetPhysicalDeviceMemoryProperties(physicalDevice, pdeviceprop);
			
			int num = pdeviceprop.memoryTypeCount();
			int filter = req.memoryTypeBits();
			int index = 0;
			for(int i = 0; i < num; i++) {
				if((filter & (1 << i)) != 0 && (pdeviceprop.memoryTypes(i).propertyFlags() & prop) != 0) {
					index = i; break;
				}
			}
			
			VkMemoryAllocateInfo meminfo = VkMemoryAllocateInfo.callocStack(stack)
			.sType(VK_STRUCTURE_TYPE_MEMORY_ALLOCATE_INFO)
			.allocationSize(req.size())
			.memoryTypeIndex(index);
			
			LongBuffer lb = stack.mallocLong(1);
			if(vkAllocateMemory(device, meminfo, null, lb) != VK_SUCCESS)
				Debug.error("API", "Allocate Memory failed!");
			if(vkBindBufferMemory(device, buffer, lb.get(0), 0) != VK_SUCCESS)
				Debug.error("API", "Bind Buffer Memory failed!");
			return lb.get(0);
		}
	}
	
	
	public long map(int size) {return map(size, memory);}
	private long map(int size, long memory) {
		try(MemoryStack stack = MemoryStack.stackPush()) {
			PointerBuffer pb = stack.mallocPointer(1);
			if(vkMapMemory(device, memory, 0, size, 0, pb) != VK_SUCCESS)
				Debug.error("API", "Memory Map failed!");
			return pb.get(0);
		}
	}
	
	public void unmap() {vkUnmapMemory(device, memory);}
	private void unmap(long memory) {vkUnmapMemory(device, memory);}
	
	public void destroy() {
		vkDestroyBuffer(device, buffer, null);
		vkFreeMemory(device, memory, null);
		buffer = memory = 0L;
	}
}
