package api.vulkan;

import static org.lwjgl.vulkan.VK10.vkEnumerateDeviceExtensionProperties;

import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.util.ArrayList;

import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.KHRSwapchain;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkDeviceCreateInfo;
import org.lwjgl.vulkan.VkDeviceQueueCreateInfo;
import org.lwjgl.vulkan.VkExtensionProperties;
import org.lwjgl.vulkan.VkFenceCreateInfo;
import org.lwjgl.vulkan.VkSemaphoreCreateInfo;

import engine.debug.Debug;
import util.Memory;

public class Device {
	public final VkDevice device;
	public Device(PhysicalDevice physicalDevice, QueueFamily queueFamily, ArrayList<String> deviceExtensions, ArrayList<String> layers) {
		try(MemoryStack stack = MemoryStack.stackPush()) {
			IntBuffer ib = stack.mallocInt(1);
			VkDeviceQueueCreateInfo.Buffer queueInfo = VkDeviceQueueCreateInfo.callocStack(1, stack)
			.sType(VK10.VK_STRUCTURE_TYPE_DEVICE_QUEUE_CREATE_INFO)
			.queueFamilyIndex(queueFamily.index)
			.pQueuePriorities(stack.floats(0f));
			
			VK10.vkEnumerateDeviceExtensionProperties(physicalDevice.device, (String)null, ib, null);
			VkExtensionProperties.Buffer device_extensions = VkExtensionProperties.mallocStack(ib.get(0), stack);
	        vkEnumerateDeviceExtensionProperties(physicalDevice.device, (String)null, ib, device_extensions);
			
	        deviceExtensions.add(KHRSwapchain.VK_KHR_SWAPCHAIN_EXTENSION_NAME);
	        
			VkDeviceCreateInfo deviceInfo = VkDeviceCreateInfo.callocStack(stack)
			.sType(VK10.VK_STRUCTURE_TYPE_DEVICE_CREATE_INFO)
			.pQueueCreateInfos(queueInfo)
			.pEnabledFeatures(physicalDevice.features)
			.ppEnabledExtensionNames(Memory.stackPointers(stack, deviceExtensions))
			.ppEnabledLayerNames(Memory.stackPointers(stack, layers));
			
			PointerBuffer pb = stack.mallocPointer(1);
			VK10.vkCreateDevice(physicalDevice.device, deviceInfo, null, pb);
			device = new VkDevice(pb.get(0), physicalDevice.device, deviceInfo);
			
			if(device == null) Debug.error("API", "Failed to create vulkan device!");
		}
	}
	
	////SEMAPHORES\\\\
	private static final VkSemaphoreCreateInfo SEMAPHORE_INFO = VkSemaphoreCreateInfo.calloc().sType(VK10.VK_STRUCTURE_TYPE_SEMAPHORE_CREATE_INFO);
	public long createSemaphore() {try(MemoryStack stack = MemoryStack.stackPush()) {return createSemaphore(stack.mallocLong(1));}}
	public long createSemaphore(MemoryStack stack) {return createSemaphore(stack.mallocLong(1));}
	public long createSemaphore(LongBuffer lb) {
		if(VK10.vkCreateSemaphore(device, SEMAPHORE_INFO, null, lb) != VK10.VK_SUCCESS) {
			Debug.error("API", "Vulkan Semaphore failed to create!");
		}
		return lb.get(0);
	}
	
	public long[] createSemaphore(int num) {try(MemoryStack stack = MemoryStack.stackPush()) {return createSemaphore(num, stack.mallocLong(num));}}
	public long[] createSemaphore(int num, MemoryStack stack) {return createSemaphore(num, stack.mallocLong(num));}
	public long[] createSemaphore(int num, LongBuffer lb) {
		long[] out = new long[num];
		for(int i = 0; i < num; i++) {
			if(VK10.vkCreateSemaphore(device, SEMAPHORE_INFO, null, lb) != VK10.VK_SUCCESS) Debug.error("API", "Vulkan Semaphore failed to create!");
			out[i] = lb.get(0);
		}
		return out;
	}
	
	public void destroySemaphore(long semaphore) {VK10.vkDestroySemaphore(device, semaphore, null);}
	public void destroySemaphore(long[] semaphore) {
		for(int i = 0; i < semaphore.length; i++) VK10.vkDestroySemaphore(device, semaphore[i], null);
	}
	
	////FENCES\\\\
	private static final VkFenceCreateInfo FENCE_INFO = VkFenceCreateInfo.calloc().sType(VK10.VK_STRUCTURE_TYPE_FENCE_CREATE_INFO).flags(VK10.VK_FENCE_CREATE_SIGNALED_BIT);
	public long createFence() {try(MemoryStack stack = MemoryStack.stackPush()) {return createFence(stack.mallocLong(1));}}
	public long createFence(MemoryStack stack) {return createFence(stack.mallocLong(1));}
	public long createFence(LongBuffer lb) {
		if(VK10.vkCreateFence(device, FENCE_INFO, null, lb) != VK10.VK_SUCCESS) {
			Debug.error("API", "Failed to create Fence!");
		}
		return lb.get(0);
	}
	
	public long[] createFence(int num) {try(MemoryStack stack = MemoryStack.stackPush()) {return createFence(num, stack.mallocLong(num));}}
	public long[] createFence(int num, MemoryStack stack) {return createFence(num, stack.mallocLong(num));}
	public long[] createFence(int num, LongBuffer lb) {
		long[] out = new long[num];
		for(int i = 0; i < num; i++) {
			if(VK10.vkCreateFence(device, FENCE_INFO, null, lb) != VK10.VK_SUCCESS) Debug.error("API", "Failed to create Fence!");
			out[i] = lb.get(0);
		}
		return out;
	}
	
	public void destroyFence(long fence) {VK10.vkDestroyFence(device, fence, null);}
	public void destroyFence(long[] fence) {for(int i = 0; i < fence.length; i++) VK10.vkDestroyFence(device, fence[i], null);}
	
	public int waitIdle() {return VK10.vkDeviceWaitIdle(device);}
	
	////DISPOSE\\\\
	public void dispose() {VK10.vkDestroyDevice(device, null);}
}
