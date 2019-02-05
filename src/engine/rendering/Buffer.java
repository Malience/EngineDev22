package engine.rendering;

import java.nio.FloatBuffer;
import java.nio.LongBuffer;

import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkBufferCreateInfo;
import org.lwjgl.vulkan.VkMemoryAllocateInfo;
import org.lwjgl.vulkan.VkMemoryRequirements;
import org.lwjgl.vulkan.VkPhysicalDeviceMemoryProperties;

import api.vulkan.Device;
import api.vulkan.PhysicalDevice;
import api.vulkan.Vertex;

public class Buffer {
	public long buffer;
	
//	public void Buffer(PhysicalDevice physicalDevice, Device device) {
//		try(MemoryStack stack = MemoryStack.stackPush()) {
//			VkBufferCreateInfo bufferInfo = VkBufferCreateInfo.callocStack(stack)
//			.sType(VK10.VK_STRUCTURE_TYPE_BUFFER_CREATE_INFO)
//			.size(vertices.length * Vertex.SIZEOF)
//			.usage(VK10.VK_BUFFER_USAGE_VERTEX_BUFFER_BIT)
//			.sharingMode(VK10.VK_SHARING_MODE_EXCLUSIVE);
//			
//			LongBuffer lb = stack.mallocLong(1);
//			
//			VK10.vkCreateBuffer(device.device, bufferInfo, null, lb);
//			buffer = lb.get(0);
//			
//			
//			VkMemoryRequirements bufferMem;
//			VK10.vkGetBufferMemoryRequirements(device.device, buffer, (bufferMem = VkMemoryRequirements.calloc()));
//			
//			VkPhysicalDeviceMemoryProperties pdeviceprop = VkPhysicalDeviceMemoryProperties.callocStack(stack);
//			VK10.vkGetPhysicalDeviceMemoryProperties(physicalDevice.device, pdeviceprop);
//			
//			int num = pdeviceprop.memoryTypeCount();
//			int filter = bufferMem.memoryTypeBits(), prop = VK10.VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT | VK10.VK_MEMORY_PROPERTY_HOST_COHERENT_BIT;
//			int index = 0;
//			for(int i = 0; i < num; i++) {
//				if((filter & (1 << i)) != 0 && (pdeviceprop.memoryTypes(i).propertyFlags() & prop) != 0) {
//					index = i; break;
//				}
//			}
//			
//			VkMemoryAllocateInfo meminfo = VkMemoryAllocateInfo.callocStack(stack)
//			.sType(VK10.VK_STRUCTURE_TYPE_MEMORY_ALLOCATE_INFO)
//			.allocationSize(bufferMem.size())
//			.memoryTypeIndex(index);
//			
//			VK10.vkAllocateMemory(device.device, meminfo, null, lb);
//			deviceMem = lb.get(0);
//			
//			VK10.vkBindBufferMemory(device.device, buffer, deviceMem, 0);
//			
//			PointerBuffer pb = stack.mallocPointer(1);
//			
//			VK10.vkMapMemory(device.device, deviceMem, 0, bufferInfo.size(), 0, pb);
//			long pointer = pb.get(0);
//			FloatBuffer fb = MemoryUtil.memFloatBuffer(pointer, (int) bufferInfo.size() / 4);
//			for(Vertex v : vertices) {
//				fb.put(v.pos.x()); fb.put(v.pos.y());
//				fb.put(v.color.x()); fb.put(v.color.y()); fb.put(v.color.z());
//			}
//			fb.flip();
//			VK10.vkUnmapMemory(device.device, deviceMem);
//		}
//	}
}
