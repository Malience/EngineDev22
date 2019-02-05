package api.vulkan;

import static org.lwjgl.vulkan.VK10.VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_SUBMIT_INFO;
import static org.lwjgl.vulkan.VK10.vkQueueSubmit;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;

import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.KHRSwapchain;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkPresentInfoKHR;
import org.lwjgl.vulkan.VkQueue;
import org.lwjgl.vulkan.VkSubmitInfo;

public class Queue {
	public final VkQueue queue;
	
	public Queue(Device device, QueueFamily queueFamily) {this(device, queueFamily, 0);}
	public Queue(Device device, QueueFamily queueFamily, int i) {
		try(MemoryStack stack = MemoryStack.stackPush()) {
			PointerBuffer pb = stack.mallocPointer(1);
			VK10.vkGetDeviceQueue(device.device, queueFamily.index, i, pb);
			queue = new VkQueue(pb.get(0), device.device);
		}
	}
	
	public int submit(QueueSubmitInfo info) {return vkQueueSubmit(queue, info, 0L);}
	public int submit(QueueSubmitInfo info, long fence) {return vkQueueSubmit(queue, info, fence);}
	public int submit(CommandBuffer buffer) {return submit(buffer, 0L);}
	public int submit(CommandBuffer buffer, long fence) {
		try(MemoryStack stack = MemoryStack.stackPush()) {
			VkSubmitInfo info = VkSubmitInfo.callocStack(stack)
			.sType(VK_STRUCTURE_TYPE_SUBMIT_INFO)
			.pCommandBuffers(stack.pointers(buffer));
			
			return vkQueueSubmit(queue, info, fence);
		}
	}
	
	public int present(MemoryStack stack, long swapchain, long wait, int index) {
		return present(stack, stack.longs(swapchain), stack.longs(wait), stack.ints(index));}
	public int present(MemoryStack stack, long[] swapchains, long[] wait, int[] indices) {
		return present(stack, stack.longs(swapchains), stack.longs(wait), stack.ints(indices));}
	public int present(MemoryStack stack, LongBuffer swapchains, LongBuffer wait, IntBuffer indices) {
		VkPresentInfoKHR presentinfo = VkPresentInfoKHR.callocStack(stack)
		.sType(KHRSwapchain.VK_STRUCTURE_TYPE_PRESENT_INFO_KHR)
		.pWaitSemaphores(wait)
		.swapchainCount(swapchains.limit())
		.pSwapchains(swapchains)
		.pImageIndices(indices)
		.pResults(null);
		return KHRSwapchain.vkQueuePresentKHR(queue, presentinfo);
	}
	
	public int waitIdle() {return VK10.vkQueueWaitIdle(queue);}
}
