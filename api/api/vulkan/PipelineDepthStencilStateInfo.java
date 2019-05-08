package api.vulkan;

import java.nio.ByteBuffer;

import static org.lwjgl.system.MemoryStack.stackGet;
import static org.lwjgl.system.MemoryUtil.*;
import static org.lwjgl.vulkan.VK10.*;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkPipelineDepthStencilStateCreateInfo;

public class PipelineDepthStencilStateInfo extends VkPipelineDepthStencilStateCreateInfo {
	public PipelineDepthStencilStateInfo(ByteBuffer container) {
		super(container);
		init();
	}
	
//	VkPipelineDepthStencilStateCreateInfo depthInfo = VkPipelineDepthStencilStateCreateInfo.callocStack(stack)
//			.sType(VK10.VK_STRUCTURE_TYPE_PIPELINE_DEPTH_STENCIL_STATE_CREATE_INFO)
//			.depthTestEnable(true)
//			.depthWriteEnable(true)
//			.depthCompareOp(VK10.VK_COMPARE_OP_LESS)
//			.depthBoundsTestEnable(false)
//			.minDepthBounds(0.0f)
//			.maxDepthBounds(1.0f)
//			.stencilTestEnable(false);
	
	public PipelineDepthStencilStateInfo depthBounds(float min, float max) {this.minDepthBounds(min).maxDepthBounds(max); return this;}
	public PipelineDepthStencilStateInfo depthTest(boolean enable) {this.depthTestEnable(enable); return this;}
	
	private PipelineDepthStencilStateInfo init() {sType(VK_STRUCTURE_TYPE_PIPELINE_DEPTH_STENCIL_STATE_CREATE_INFO); return this;}
	
	//Off Stack Allocation
	public static PipelineDepthStencilStateInfo malloc() {return wrap(PipelineDepthStencilStateInfo.class, nmemAllocChecked(SIZEOF)).init();}
    public static PipelineDepthStencilStateInfo calloc() {return wrap(PipelineDepthStencilStateInfo.class, nmemCallocChecked(1, SIZEOF)).init();}
    //Stack Buffer Allocation
    public static PipelineDepthStencilStateInfo mallocStack() {return mallocStack(stackGet());}
    public static PipelineDepthStencilStateInfo callocStack() {return callocStack(stackGet());}
    public static PipelineDepthStencilStateInfo mallocStack(MemoryStack stack) {return wrap(PipelineDepthStencilStateInfo.class, stack.nmalloc(ALIGNOF, SIZEOF)).init();}
    public static PipelineDepthStencilStateInfo callocStack(MemoryStack stack) {return wrap(PipelineDepthStencilStateInfo.class, stack.ncalloc(ALIGNOF, 1, SIZEOF)).init();}
}
