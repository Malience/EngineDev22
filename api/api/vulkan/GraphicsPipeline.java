package api.vulkan;

import static org.lwjgl.vulkan.VK10.*;

import java.nio.ByteBuffer;
import java.nio.LongBuffer;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkExtent2D;
import org.lwjgl.vulkan.VkGraphicsPipelineCreateInfo;
import org.lwjgl.vulkan.VkPipelineColorBlendAttachmentState;
import org.lwjgl.vulkan.VkPipelineColorBlendStateCreateInfo;
import org.lwjgl.vulkan.VkPipelineDepthStencilStateCreateInfo;
import org.lwjgl.vulkan.VkPipelineInputAssemblyStateCreateInfo;
import org.lwjgl.vulkan.VkPipelineLayoutCreateInfo;
import org.lwjgl.vulkan.VkPipelineMultisampleStateCreateInfo;
import org.lwjgl.vulkan.VkPipelineRasterizationStateCreateInfo;
import org.lwjgl.vulkan.VkPipelineShaderStageCreateInfo;
import org.lwjgl.vulkan.VkPipelineVertexInputStateCreateInfo;
import org.lwjgl.vulkan.VkPipelineViewportStateCreateInfo;
import org.lwjgl.vulkan.VkRect2D;
import org.lwjgl.vulkan.VkViewport;

public class GraphicsPipeline {// extends VkGraphicsPipelineCreateInfo.Buffer {
	public static final int 
		POINT_LIST = VK_PRIMITIVE_TOPOLOGY_POINT_LIST,
		LINE_LIST = VK_PRIMITIVE_TOPOLOGY_LINE_LIST,
		LINE_STRIP = VK_PRIMITIVE_TOPOLOGY_LINE_STRIP,
		TRIANGLE_LIST = VK_PRIMITIVE_TOPOLOGY_TRIANGLE_LIST,
		TRIANGLE_STRIP = VK_PRIMITIVE_TOPOLOGY_TRIANGLE_STRIP,
		TRIANGLE_FAN = VK_PRIMITIVE_TOPOLOGY_TRIANGLE_FAN;
	
	private VkDevice device;
//	private LongBuffer pipeline;
	
	public long pipeline;
	public VkExtent2D extent;
	public long layout;
	
//	public GraphicsPipeline(Device device) {
//		super(MemoryUtil.memCalloc(1, VkGraphicsPipelineCreateInfo.SIZEOF));
//		pipeline = MemoryUtil.memAllocLong(1);
//		//this.device = device.device;
//		
//		sType(VK_STRUCTURE_TYPE_GRAPHICS_PIPELINE_CREATE_INFO);
//		pVertexInputState(VkPipelineVertexInputStateCreateInfo.calloc().sType(VK_STRUCTURE_TYPE_PIPELINE_VERTEX_INPUT_STATE_CREATE_INFO));
//		//pTessellationState().sType(VK_STRUCTURE_TYPE_PIPELINE_TESSELLATION_STATE_CREATE_INFO);
//		pViewportState(VkPipelineViewportStateCreateInfo.calloc().sType(VK_STRUCTURE_TYPE_PIPELINE_VIEWPORT_STATE_CREATE_INFO));
//		pRasterizationState(VkPipelineRasterizationStateCreateInfo.calloc().sType(VK_STRUCTURE_TYPE_PIPELINE_RASTERIZATION_STATE_CREATE_INFO));
//		pMultisampleState(VkPipelineMultisampleStateCreateInfo.calloc().sType(VK_STRUCTURE_TYPE_PIPELINE_MULTISAMPLE_STATE_CREATE_INFO));
//		pDepthStencilState(VkPipelineDepthStencilStateCreateInfo.calloc().sType(VK_STRUCTURE_TYPE_PIPELINE_DEPTH_STENCIL_STATE_CREATE_INFO));
//	}
	
	
	
	public GraphicsPipeline(Device device, Swapchain swapchain, Renderpass renderpass, DescriptorLayout desLayout, Shader... shaders) {
		try(MemoryStack stack = MemoryStack.stackPush()) {
			this.device = device.device;
			LongBuffer lb = stack.mallocLong(1);
			
			VkPipelineDepthStencilStateCreateInfo depthInfo = VkPipelineDepthStencilStateCreateInfo.callocStack(stack)
			.sType(VK10.VK_STRUCTURE_TYPE_PIPELINE_DEPTH_STENCIL_STATE_CREATE_INFO)
			.depthTestEnable(true)
			.depthWriteEnable(true)
			.depthCompareOp(VK10.VK_COMPARE_OP_LESS_OR_EQUAL)
			.depthBoundsTestEnable(false)
			.minDepthBounds(0.0f)
			.maxDepthBounds(1.0f)
			.stencilTestEnable(false);
			
			//Pipeline shader stages setup
			ByteBuffer main = stack.ASCII("main");
			int length = shaders.length;
			VkPipelineShaderStageCreateInfo.Buffer stages = VkPipelineShaderStageCreateInfo.callocStack(length, stack);
			for(int i = 0; i < length; i++) {
				Shader shader = shaders[i];
				stages.get(i).sType(VK_STRUCTURE_TYPE_PIPELINE_SHADER_STAGE_CREATE_INFO)
				.stage(shader.stage)
				.module(shader.module)
				.pName(main);
			}
			
			VkPipelineVertexInputStateCreateInfo vertexInput = VkPipelineVertexInputStateCreateInfo.callocStack(stack)
			.sType(VK_STRUCTURE_TYPE_PIPELINE_VERTEX_INPUT_STATE_CREATE_INFO)
			.pVertexBindingDescriptions(Vertex.getBindingDescription(stack))
			.pVertexAttributeDescriptions(Vertex.getAttributeDescriptions(stack));
			
			VkPipelineInputAssemblyStateCreateInfo assemblyState = VkPipelineInputAssemblyStateCreateInfo.callocStack(stack)
			.sType(VK_STRUCTURE_TYPE_PIPELINE_INPUT_ASSEMBLY_STATE_CREATE_INFO)
			.topology(VK10.VK_PRIMITIVE_TOPOLOGY_TRIANGLE_LIST)
			.primitiveRestartEnable(false);
			
			VkViewport.Buffer viewport = VkViewport.callocStack(1, stack)
			.x(0).y(0)
			.width(swapchain.extent.width()).height(swapchain.extent.height())
			.minDepth(0).maxDepth(1.0f);
			
			VkRect2D.Buffer scissor = VkRect2D.callocStack(1, stack);
			scissor.offset().set(0, 0);
			scissor.extent().set(swapchain.extent);
			extent = swapchain.extent;
			
			VkPipelineViewportStateCreateInfo viewportState = VkPipelineViewportStateCreateInfo.callocStack(stack)
			.sType(VK_STRUCTURE_TYPE_PIPELINE_VIEWPORT_STATE_CREATE_INFO)
			.viewportCount(1)
			.pViewports(viewport)
			.pScissors(scissor);
			
			VkPipelineRasterizationStateCreateInfo rasterizer = VkPipelineRasterizationStateCreateInfo.callocStack(stack)
			.sType(VK_STRUCTURE_TYPE_PIPELINE_RASTERIZATION_STATE_CREATE_INFO)
			.depthClampEnable(false)
			.rasterizerDiscardEnable(false)
			.polygonMode(VK10.VK_POLYGON_MODE_FILL)
			.lineWidth(1.0f)
			.cullMode(VK10.VK_CULL_MODE_BACK_BIT)
			//.frontFace(VK10.VK_FRONT_FACE_CLOCKWISE)
			.frontFace(VK10.VK_FRONT_FACE_COUNTER_CLOCKWISE)
			.depthBiasEnable(false);
			
			VkPipelineMultisampleStateCreateInfo multisampling = VkPipelineMultisampleStateCreateInfo.callocStack(stack)
			.sType(VK_STRUCTURE_TYPE_PIPELINE_MULTISAMPLE_STATE_CREATE_INFO)
			.sampleShadingEnable(false)
			.rasterizationSamples(VK10.VK_SAMPLE_COUNT_1_BIT);
			
			VkPipelineColorBlendAttachmentState.Buffer colorBlendAttachment = VkPipelineColorBlendAttachmentState.callocStack(1, stack)
			.colorWriteMask(VK_COLOR_COMPONENT_R_BIT | VK_COLOR_COMPONENT_G_BIT | VK_COLOR_COMPONENT_B_BIT | VK_COLOR_COMPONENT_A_BIT)
			.blendEnable(false);
			
			VkPipelineColorBlendStateCreateInfo colorBlending = VkPipelineColorBlendStateCreateInfo.callocStack(stack)
			.sType(VK_STRUCTURE_TYPE_PIPELINE_COLOR_BLEND_STATE_CREATE_INFO)
			.logicOpEnable(false)
			.pAttachments(colorBlendAttachment);
			
			VkPipelineLayoutCreateInfo pipelineLayoutInfo = VkPipelineLayoutCreateInfo.callocStack(stack)
			.sType(VK_STRUCTURE_TYPE_PIPELINE_LAYOUT_CREATE_INFO)
			.pSetLayouts(stack.longs(desLayout.layout));
			
			vkCreatePipelineLayout(this.device, pipelineLayoutInfo, null, lb);
			
			VkGraphicsPipelineCreateInfo.Buffer pipelineInfo = VkGraphicsPipelineCreateInfo.callocStack(1, stack)
			.sType(VK_STRUCTURE_TYPE_GRAPHICS_PIPELINE_CREATE_INFO)
			.pStages(stages)
			.pVertexInputState(vertexInput)
			.pInputAssemblyState(assemblyState)
			.pViewportState(viewportState)
			.pRasterizationState(rasterizer)
			.pMultisampleState(multisampling)
			.pColorBlendState(colorBlending)
			.pDepthStencilState(depthInfo)
			.layout((layout = lb.get(0)))
			.renderPass(renderpass.renderpass)
			.subpass(0);
			
			VK10.vkCreateGraphicsPipelines(this.device, MemoryUtil.NULL, pipelineInfo, null, lb);
			pipeline = lb.get(0);
		}
		
	}
	
	public void dispose() {
		VK10.vkDestroyPipelineLayout(this.device, layout, null);
		VK10.vkDestroyPipeline(this.device, pipeline, null);
	}
	
}
