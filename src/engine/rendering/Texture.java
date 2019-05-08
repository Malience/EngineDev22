package engine.rendering;

import static org.lwjgl.vulkan.VK10.*;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;

import org.lwjgl.PointerBuffer;
import org.lwjgl.stb.STBImage;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.vulkan.VkDevice;

import api.vulkan.BufferObject;
import api.vulkan.CommandBuffer;
import api.vulkan.CommandPool;
import api.vulkan.Device;
import api.vulkan.Image;
import api.vulkan.PhysicalDevice;
import api.vulkan.Queue;

public class Texture {
	private static final String TEXTURE_LOCATION = "./res/textures/";
	private final VkDevice device;
	
	public long image, imageMemory, imageView;
	
	public Texture(PhysicalDevice physicalDevice, Device device, CommandPool commandPool, Queue queue, String filename) {
		try(MemoryStack stack = MemoryStack.stackPush()) {
			this.device = device.device;
			LongBuffer lb = stack.callocLong(2);
			PointerBuffer pb = stack.mallocPointer(1);
			IntBuffer widthBuffer = stack.mallocInt(1), heightBuffer = stack.mallocInt(1), channelsBuffer = stack.mallocInt(1);
			ByteBuffer texureData = STBImage.stbi_load(TEXTURE_LOCATION + filename, widthBuffer, heightBuffer, channelsBuffer, STBImage.STBI_rgb_alpha);
			int width = widthBuffer.get(0), height = heightBuffer.get(0), channels = channelsBuffer.get(0);
			int textureSize = width * height * 4;
			
			long textureStagingBuffer;
			long textureStagingMemory;
			
			BufferObject.createBuffer(physicalDevice, device, textureSize, VK_BUFFER_USAGE_TRANSFER_SRC_BIT, VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT | VK_MEMORY_PROPERTY_HOST_COHERENT_BIT, lb);
			
			textureStagingBuffer = lb.get(0);
			textureStagingMemory = lb.get(1);
			
			vkMapMemory(this.device, textureStagingMemory, 0L, textureSize, 0, pb);
			MemoryUtil.memCopy(MemoryUtil.memAddress(texureData), pb.get(0), textureSize); //PossibleIssue
			vkUnmapMemory(this.device, textureStagingMemory);
			
			STBImage.stbi_image_free(texureData);
			
			int format = VK_FORMAT_R8G8B8A8_UNORM;
			
			
			Image.createImage(physicalDevice, device, width, height, format, VK_IMAGE_TILING_OPTIMAL, VK_IMAGE_USAGE_TRANSFER_DST_BIT | VK_IMAGE_USAGE_SAMPLED_BIT, VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT, lb);
			
			image = lb.get(0);
			imageMemory = lb.get(1);
			
			CommandBuffer commandBuffer = commandPool.createBuffer();
			commandBuffer.beginOnce();
			
			commandBuffer.transitionImageLayout(image, format, VK_IMAGE_LAYOUT_UNDEFINED, VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL, VK_PIPELINE_STAGE_TOP_OF_PIPE_BIT, VK_PIPELINE_STAGE_TRANSFER_BIT);
			commandBuffer.copyBufferToImage(textureStagingBuffer, image, width, height);
			commandBuffer.transitionImageLayout(image, format, VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL, VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL, VK_ACCESS_TRANSFER_WRITE_BIT, VK_ACCESS_SHADER_READ_BIT);
			
			commandBuffer.end();
			
			queue.submit(commandBuffer);
			commandPool.destroyBuffer(commandBuffer);
			
			vkDestroyBuffer(this.device, textureStagingBuffer, null);
			vkFreeMemory(this.device, textureStagingMemory, null);
			
			imageView = Image.createImageView(device, image, format);
		}
	}
	
	public void dispose() {
		vkDestroyImageView(this.device, imageView, null);
		
		vkDestroyImage(this.device, image, null);
		vkFreeMemory(this.device, imageMemory, null);
	}
	
}
