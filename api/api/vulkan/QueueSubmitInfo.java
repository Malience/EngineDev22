package api.vulkan;

import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_SUBMIT_INFO;

import java.nio.ByteBuffer;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.vulkan.VkCommandBuffer;
import org.lwjgl.vulkan.VkSubmitInfo;

public class QueueSubmitInfo extends VkSubmitInfo {
	//Constructors
	private QueueSubmitInfo(ByteBuffer container) {super(container);}
	public QueueSubmitInfo() {
		super(MemoryUtil.memCalloc(1, VkSubmitInfo.SIZEOF));
		this.sType(VK_STRUCTURE_TYPE_SUBMIT_INFO);
	}
	
	//Wait Semaphores
	public void pWaitSemaphores(long... value) {try(MemoryStack stack = MemoryStack.stackPush()){this.pWaitSemaphores(stack, value);}}
	public void pWaitSemaphores(MemoryStack stack, long... value) {this.pWaitSemaphores(stack.longs(value));}
	
	//Destination Stage Mask
	public void pWaitDstStageMask(int... value) {try(MemoryStack stack = MemoryStack.stackPush()){this.pWaitDstStageMask(stack, value);}}
	public void pWaitDstStageMask(MemoryStack stack, int... value) {this.pWaitDstStageMask(stack.ints(value));}
	
	//Command Buffers
	public void pCommandBuffers(VkCommandBuffer... value) {try(MemoryStack stack = MemoryStack.stackPush()){this.pCommandBuffers(stack, value);}}
	public void pCommandBuffers(MemoryStack stack, VkCommandBuffer... value) {this.pCommandBuffers(stack.pointers(value));}
	
	//Signal Semaphores
	public void pSignalSemaphores(long... value) {try(MemoryStack stack = MemoryStack.stackPush()){this.pSignalSemaphores(stack, value);}}
	public void pSignalSemaphores(MemoryStack stack, long... value) {this.pSignalSemaphores(stack.longs(value));}
	
	//Buffer
	public static class Buffer extends VkSubmitInfo.Buffer {
		private Buffer(ByteBuffer container) {super(container);}
		public Buffer(int size) {
			super(__create(size, SIZEOF));
		}
		public Buffer(QueueSubmitInfo... info) {
			super(__create(info.length, SIZEOF));
			for(int i = 0; i < info.length; i++) this.put(i, info[i]);
		}
	}
}
