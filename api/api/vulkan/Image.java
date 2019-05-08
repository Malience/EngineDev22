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
import org.lwjgl.vulkan.VkSamplerCreateInfo;

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
	
	public static void createImage(PhysicalDevice physicalDevice, Device device, int width, int height, int format, int tiling, int usage, int properties, LongBuffer lb) {
		try(MemoryStack stack = MemoryStack.stackPush()) {
			VkImageCreateInfo imageCreateInfo = VkImageCreateInfo.callocStack(stack)
			.sType(VK_STRUCTURE_TYPE_IMAGE_CREATE_INFO)
			.imageType(VK_IMAGE_TYPE_2D)
			.mipLevels(1)
			.arrayLayers(1)
			.format(format)
			.tiling(tiling)
			.initialLayout(VK_IMAGE_LAYOUT_UNDEFINED)
			.usage(usage)
			.sharingMode(VK_SHARING_MODE_EXCLUSIVE)
			.samples(VK_SAMPLE_COUNT_1_BIT)
			.flags(0);
			imageCreateInfo.extent()
			.width(width)
			.height(height)
			.depth(1);
			
			lb.position(0);
			int result = vkCreateImage(device.device, imageCreateInfo, null, lb);
			if(result != VK_SUCCESS)
				Debug.error("API", "Image creation failed!");
			
			VkMemoryRequirements memoryRequirements = VkMemoryRequirements.callocStack(stack);
			vkGetImageMemoryRequirements(device.device, lb.get(0), memoryRequirements);
			
			VkMemoryAllocateInfo allocateInfo = VkMemoryAllocateInfo.callocStack(stack)
			.sType(VK_STRUCTURE_TYPE_MEMORY_ALLOCATE_INFO)
			.allocationSize(memoryRequirements.size())
			.memoryTypeIndex(PhysicalDevice.findMemoryType(physicalDevice, memoryRequirements.memoryTypeBits(), VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT));
			
			lb.position(1);
			if(vkAllocateMemory(device.device, allocateInfo, null, lb) != VK_SUCCESS)
				Debug.error("API", "Image memory allocation failed!");
			
			vkBindImageMemory(device.device, lb.get(0), lb.get(1), 0L);
			lb.position(0);
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
	
	public static long createSampler(Device device) {
		try(MemoryStack stack = MemoryStack.stackPush()) {
			LongBuffer lb = stack.mallocLong(1);
			VkSamplerCreateInfo samplerInfo = VkSamplerCreateInfo.callocStack(stack)
			.sType(VK_STRUCTURE_TYPE_SAMPLER_CREATE_INFO)
			.magFilter(VK_FILTER_LINEAR)
			.minFilter(VK_FILTER_LINEAR)
			.addressModeU(VK_SAMPLER_ADDRESS_MODE_REPEAT)
			.addressModeV(VK_SAMPLER_ADDRESS_MODE_REPEAT)
			.addressModeW(VK_SAMPLER_ADDRESS_MODE_REPEAT)
			.anisotropyEnable(true)
			.maxAnisotropy(16)
			.borderColor(VK_BORDER_COLOR_INT_OPAQUE_BLACK)
			.unnormalizedCoordinates(false)
			.compareEnable(false)
			.compareOp(VK_COMPARE_OP_ALWAYS)
			.mipmapMode(VK_SAMPLER_MIPMAP_MODE_LINEAR)
			.mipLodBias(0f)
			.minLod(0f)
			.maxLod(0f);
			
			if(vkCreateSampler(device.device, samplerInfo, null, lb) != VK_SUCCESS)
				Debug.error("API", "Sampler creation failed!");
			return lb.get(0);
		}
	}
	
	public static void transition(CommandBuffer cb, Queue queue, long image, int format, int srcLayout, int dstlayout, int srcStage, int dstStage) {
		try(MemoryStack stack = MemoryStack.stackPush()) {
			cb.beginOnce();
			cb.transitionImageLayout(image, format, srcLayout, dstlayout, srcStage, dstStage);
			cb.end();
			queue.submit(cb);
		}
	}
}
