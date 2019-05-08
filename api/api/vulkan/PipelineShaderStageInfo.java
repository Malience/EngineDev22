package api.vulkan;

import static org.lwjgl.system.MemoryStack.stackGet;
import static org.lwjgl.system.MemoryUtil.nmemCallocChecked;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_PIPELINE_SHADER_STAGE_CREATE_INFO;
import static org.lwjgl.vulkan.VkPipelineShaderStageCreateInfo.SIZEOF;
import static org.lwjgl.vulkan.VkPipelineShaderStageCreateInfo.ALIGNOF;

import java.nio.ByteBuffer;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.vulkan.VkGraphicsPipelineCreateInfo;
import org.lwjgl.vulkan.VkPipelineShaderStageCreateInfo;

public class PipelineShaderStageInfo extends VkPipelineShaderStageCreateInfo.Buffer {
	//Parent Constructors
	PipelineShaderStageInfo(ByteBuffer container) {super(container);}
	PipelineShaderStageInfo(long address, int cap) {super(address, cap);}
	
	public PipelineShaderStageInfo(int number) {super(MemoryUtil.memCalloc(number, VkGraphicsPipelineCreateInfo.SIZEOF));}
	public PipelineShaderStageInfo(Shader... shaders) {
		super(MemoryUtil.memCalloc(shaders.length, VkGraphicsPipelineCreateInfo.SIZEOF));
		setShaders(shaders);
	}

	public PipelineShaderStageInfo setShaders(Shader... shaders) {
		try(MemoryStack stack = MemoryStack.stackPush()) {
			//Pipeline shader stages setup
			ByteBuffer main = stack.ASCII("main");
			int length = shaders.length;
			for(int i = 0; i < length; i++) {
				Shader shader = shaders[i];
				this.get(i).sType(VK_STRUCTURE_TYPE_PIPELINE_SHADER_STAGE_CREATE_INFO)
				.stage(shader.stage)
				.module(shader.module)
				.pName(main);
			}
		}
		return this;
	}
	
    //Buffer Allocation
    //public static PipelineShaderStageInfo malloc(int capacity) {return wrap(PipelineShaderStageInfo.class, MemoryUtil.nmemAllocChecked(MemoryUtil.));}
    public static PipelineShaderStageInfo calloc(int capacity) {return wrap(PipelineShaderStageInfo.class, nmemCallocChecked(capacity, SIZEOF), capacity);}
    //Stack Buffer Allocation
    public static PipelineShaderStageInfo mallocStack(int capacity) {return mallocStack(capacity, stackGet());}
    public static PipelineShaderStageInfo callocStack(int capacity) {return callocStack(capacity, stackGet());}
    public static PipelineShaderStageInfo mallocStack(int capacity, MemoryStack stack) {return wrap(PipelineShaderStageInfo.class, stack.nmalloc(ALIGNOF, capacity * SIZEOF), capacity);}
    public static PipelineShaderStageInfo callocStack(int capacity, MemoryStack stack) {return wrap(PipelineShaderStageInfo.class, stack.ncalloc(ALIGNOF, capacity, SIZEOF), capacity);}
}
