package api.vulkan;

import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkQueueFamilyProperties;

public class QueueFamily {
	public final int index;
	public final VkQueueFamilyProperties properties;
	
	public final int count;
	public final int flags;
	public final int timestampValidBits;
	
	public final boolean graphics, compute, transfer;
	
	
	public QueueFamily(int index, VkQueueFamilyProperties properties) {
		this.index = index;
		this.properties = properties;
		
		count = properties.queueCount();
		flags = properties.queueFlags();
		timestampValidBits = properties.timestampValidBits();
		
		graphics = (flags & VK10.VK_QUEUE_GRAPHICS_BIT) != 0;
		compute = (flags & VK10.VK_QUEUE_COMPUTE_BIT) != 0;
		transfer = (flags & VK10.VK_QUEUE_TRANSFER_BIT) != 0;
	}
	
	public static QueueFamily getGraphicsFamily(QueueFamily[] families) {
		for(QueueFamily family : families) if(family.graphics) return family;
		return null;
	}
	
	public static QueueFamily getTransferFamily(QueueFamily[] families) {
		for(QueueFamily family : families) if(!family.graphics && family.transfer) return family;
		return null;
	}
}
