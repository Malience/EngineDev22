package api.vulkan;

import org.lwjgl.PointerBuffer;
import org.lwjgl.stb.STBImage;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkBufferCreateInfo;
import org.lwjgl.vulkan.VkFormatProperties;
import org.lwjgl.vulkan.VkImageCreateInfo;
import org.lwjgl.vulkan.VkImageMemoryBarrier;
import org.lwjgl.vulkan.VkImageViewCreateInfo;
import org.lwjgl.vulkan.VkMemoryAllocateInfo;
import org.lwjgl.vulkan.VkMemoryRequirements;
import org.lwjgl.vulkan.VkPhysicalDeviceMemoryProperties;

import engine.debug.Debug;

import static org.lwjgl.vulkan.VK10.*;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;

public class Image {
	public static int findFormat(PhysicalDevice physicalDevice, int tiling, int features, int... formats) {
		try(MemoryStack stack = MemoryStack.stackPush()) {
			for(int i = 0; i < formats.length; i++) {
				VkFormatProperties properties = VkFormatProperties.callocStack(stack);
				vkGetPhysicalDeviceFormatProperties(physicalDevice.device, formats[i], properties);
				
				if(tiling == VK_IMAGE_TILING_LINEAR && (properties.linearTilingFeatures() & features) == features)
					return formats[i];
				else if(tiling == VK_IMAGE_TILING_OPTIMAL && (properties.optimalTilingFeatures() & features) == features)
					return formats[i];
			}
			return -1;
		}
	}
	
	public static long createImage(PhysicalDevice physicalDevice, Device device, int width, int height, int format) {
		try(MemoryStack stack = MemoryStack.stackPush()) {
			VkImageCreateInfo info = VkImageCreateInfo.callocStack(stack)
			.sType(VK_STRUCTURE_TYPE_IMAGE_CREATE_INFO)
			.imageType(VK_IMAGE_TYPE_2D)
			.mipLevels(1)
			.arrayLayers(1)
			.format(format)
			.tiling(VK_IMAGE_TILING_OPTIMAL)
			.initialLayout(VK_IMAGE_LAYOUT_UNDEFINED)
			.usage(VK10.VK_IMAGE_USAGE_DEPTH_STENCIL_ATTACHMENT_BIT)
			.samples(VK_SAMPLE_COUNT_1_BIT)
			.sharingMode(VK_SHARING_MODE_EXCLUSIVE)
			.pQueueFamilyIndices(null);
			info.extent().width(width).height(height).depth(1);
			
			LongBuffer lb = stack.mallocLong(1);
			if(vkCreateImage(device.device, info, null, lb) != VK_SUCCESS)
				Debug.error("API", "Could not create image!");
			return lb.get(0);
		}
	}
	
	public static long allocateImage(PhysicalDevice physicalDevice, Device device, long image, int properties) {
		try(MemoryStack stack = MemoryStack.stackPush()) {
			//Memory Requirements
			VkMemoryRequirements req = VkMemoryRequirements.mallocStack(stack);
			vkGetImageMemoryRequirements(device.device, image, req);
			//Prop and Allocate
			VkPhysicalDeviceMemoryProperties pdeviceprop = VkPhysicalDeviceMemoryProperties.mallocStack(stack);
			vkGetPhysicalDeviceMemoryProperties(physicalDevice.device, pdeviceprop);
			
			int num = pdeviceprop.memoryTypeCount();
			int filter = req.memoryTypeBits();
			int index = -1;
			for(int i = 0; i < num; i++) {
				if((filter & (1 << i)) != 0 && (pdeviceprop.memoryTypes(i).propertyFlags() & properties) != 0) {
					index = i; break;
				}
			}
			
			VkMemoryAllocateInfo meminfo = VkMemoryAllocateInfo.callocStack(stack)
			.sType(VK_STRUCTURE_TYPE_MEMORY_ALLOCATE_INFO)
			.allocationSize(req.size())
			.memoryTypeIndex(index);
			
			LongBuffer lb = stack.mallocLong(1);
			if(vkAllocateMemory(device.device, meminfo, null, lb) != VK_SUCCESS)
				Debug.error("API", "Allocate Memory failed!");
			long memory = lb.get(0);
			if(vkBindImageMemory(device.device, image, memory, 0L) != VK_SUCCESS)
				Debug.error("API", "Image could not be bound!");
			return memory;
		}
	}
	
	
	
	public static long createImageView(Device device, long image, int format) {
		try(MemoryStack stack = MemoryStack.stackPush()) {
			VkImageViewCreateInfo info = VkImageViewCreateInfo.callocStack(stack)
			.sType(VK_STRUCTURE_TYPE_IMAGE_VIEW_CREATE_INFO)
			.image(image)
			.viewType(VK_IMAGE_VIEW_TYPE_2D)
			.format(format);
			info.subresourceRange()
			.aspectMask(VK_IMAGE_ASPECT_DEPTH_BIT)
			.baseMipLevel(0)
			.levelCount(1)
			.baseArrayLayer(0)
			.layerCount(1);
			
			LongBuffer lb = stack.mallocLong(1);
			if(vkCreateImageView(device.device, info, null, lb) != VK_SUCCESS)
				Debug.error("API", "Allocate Memory failed!");
			return lb.get(0);
		}
	}
	
	
	public static void transition(CommandBuffer cb, Queue queue, long image, int format, int initialLayout, int layout) {
		try(MemoryStack stack = MemoryStack.stackPush()) {
			cb.beginOnce();
			VkImageMemoryBarrier.Buffer barrier = VkImageMemoryBarrier.callocStack(1, stack)
			.sType(VK_STRUCTURE_TYPE_IMAGE_MEMORY_BARRIER)
			.oldLayout(initialLayout)
			.newLayout(layout)
			.srcQueueFamilyIndex(VK10.VK_QUEUE_FAMILY_IGNORED)
			.dstQueueFamilyIndex(VK10.VK_QUEUE_FAMILY_IGNORED)
			.image(image)
			.srcAccessMask(0)
			.dstAccessMask(VK10.VK_ACCESS_DEPTH_STENCIL_ATTACHMENT_READ_BIT | VK_ACCESS_DEPTH_STENCIL_ATTACHMENT_WRITE_BIT);
			barrier.subresourceRange()
			.aspectMask(VK10.VK_IMAGE_ASPECT_DEPTH_BIT | VK10.VK_IMAGE_ASPECT_STENCIL_BIT);
			barrier.subresourceRange()
			.aspectMask(VK10.VK_IMAGE_ASPECT_DEPTH_BIT);
			barrier.subresourceRange()
			.baseMipLevel(0)
			.levelCount(1)
			.baseArrayLayer(0)
			.layerCount(1);
			
			vkCmdPipelineBarrier(cb, VK10.VK_PIPELINE_STAGE_TOP_OF_PIPE_BIT, VK10.VK_PIPELINE_STAGE_EARLY_FRAGMENT_TESTS_BIT, 0, null, null, barrier);
			
			cb.end();
			queue.submit(cb);
		}
	}
	
	public void createTexture(PhysicalDevice physicalDevice, Device device) {
		long texBuffer, texMemory, textureMemory, texture;
		try(MemoryStack stack = MemoryStack.stackPush()) {
			LongBuffer lb = stack.mallocLong(1);
			IntBuffer width = stack.mallocInt(1), height = stack.mallocInt(1), channels = stack.mallocInt(1);
			ByteBuffer tex = STBImage.stbi_load("./res/heightmap.png", width, height, channels, STBImage.STBI_rgb_alpha);
			int texSize = width.get(0) * height.get(0) * 4;
			//System.out.println(tex.limit());
			//Create Buffer
			VkBufferCreateInfo bufferInfo = VkBufferCreateInfo.callocStack(stack)
			.sType(VK_STRUCTURE_TYPE_BUFFER_CREATE_INFO)
			.size(texSize)
			.usage(VK_BUFFER_USAGE_TRANSFER_SRC_BIT)
			.sharingMode(VK_SHARING_MODE_EXCLUSIVE);
			
			if(vkCreateBuffer(device.device, bufferInfo, null, lb) != VK_SUCCESS)
				Debug.error("API", "Texture Buffer creation failed!");
			texBuffer = lb.get(0);
			
			//Memory Requirements
			VkMemoryRequirements req = VkMemoryRequirements.mallocStack(stack);
			vkGetBufferMemoryRequirements(device.device, texBuffer, req);
			//Prop and Allocate
			VkPhysicalDeviceMemoryProperties pdeviceprop = VkPhysicalDeviceMemoryProperties.mallocStack(stack);
			vkGetPhysicalDeviceMemoryProperties(physicalDevice.device, pdeviceprop);
			
			int num = pdeviceprop.memoryTypeCount();
			int filter = req.memoryTypeBits();
			int index = -1;
			for(int i = 0; i < num; i++) {
				if((filter & (1 << i)) != 0 && (pdeviceprop.memoryTypes(i).propertyFlags() & (VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT | VK_MEMORY_PROPERTY_HOST_COHERENT_BIT)) != 0) {
					index = i; break;
				}
			}
			//System.out.println(index);
			VkMemoryAllocateInfo meminfo = VkMemoryAllocateInfo.callocStack(stack)
			.sType(VK_STRUCTURE_TYPE_MEMORY_ALLOCATE_INFO)
			.allocationSize(req.size())
			.memoryTypeIndex(index);
			
			if(vkAllocateMemory(device.device, meminfo, null, lb) != VK_SUCCESS)
				Debug.error("API", "Allocate Memory failed!");
			if(vkBindBufferMemory(device.device, texBuffer, lb.get(0), 0) != VK_SUCCESS)
				Debug.error("API", "Bind Buffer Memory failed!");
			texMemory = lb.get(0);
			
			PointerBuffer pb = stack.mallocPointer(1);
			vkMapMemory(device.device, texMemory, 0, texSize, 0, pb);
			MemoryUtil.memCopy(MemoryUtil.memAddress(tex), pb.get(0), texSize);
			vkUnmapMemory(device.device, texMemory);
			
			STBImage.stbi_image_free(tex);
			
			VkImageCreateInfo imageInfo = VkImageCreateInfo.callocStack(stack)
			.sType(VK_STRUCTURE_TYPE_IMAGE_CREATE_INFO)
			.imageType(VK_IMAGE_TYPE_2D)
			.mipLevels(1)
			.arrayLayers(1)
			.format(VK_FORMAT_R8G8B8A8_UNORM)
			.tiling(VK_IMAGE_TILING_OPTIMAL)
			.initialLayout(VK_IMAGE_LAYOUT_UNDEFINED)
			.usage(VK_IMAGE_USAGE_TRANSFER_DST_BIT | VK_IMAGE_USAGE_SAMPLED_BIT)
			.sharingMode(VK_SHARING_MODE_EXCLUSIVE)
			.samples(VK_SAMPLE_COUNT_1_BIT)
			.flags(0);
			imageInfo.extent().width(width.get(0)).height(height.get(0)).depth(1);
			
			if(vkCreateImage(device.device, imageInfo, null, lb) != VK_SUCCESS)
				Debug.error("API", "Image failed to create!");
			texture = lb.get(0);
			
			//Memory Requirements
			vkGetImageMemoryRequirements(device.device, texture, req);
			//Prop and Allocate
			//VkPhysicalDeviceMemoryProperties pdeviceprop = VkPhysicalDeviceMemoryProperties.mallocStack(stack);
			//vkGetPhysicalDeviceMemoryProperties(physicalDevice.device, pdeviceprop);
			
			//int num = pdeviceprop.memoryTypeCount();
			filter = req.memoryTypeBits();
			index = -1;
			for(int i = 0; i < num; i++) {
				if((filter & (1 << i)) != 0 && (pdeviceprop.memoryTypes(i).propertyFlags() & VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT) != 0) {
					index = i; break;
				}
			}
			
			meminfo.allocationSize(req.size())
			.memoryTypeIndex(index);
			
			if(vkAllocateMemory(device.device, meminfo, null, lb) != VK_SUCCESS)
				Debug.error("API", "Allocate Memory failed!");
			textureMemory = lb.get(0);
			vkBindImageMemory(device.device, texture, textureMemory, 0L);
		}
	}
}
