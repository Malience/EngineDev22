package engine.rendering;

import static org.lwjgl.vulkan.VK10.*;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.util.ArrayList;

import org.lwjgl.PointerBuffer;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.stb.STBImage;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.vulkan.KHRSwapchain;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkAttachmentDescription;
import org.lwjgl.vulkan.VkAttachmentReference;
import org.lwjgl.vulkan.VkBufferCopy;
import org.lwjgl.vulkan.VkBufferCreateInfo;
import org.lwjgl.vulkan.VkBufferImageCopy;
import org.lwjgl.vulkan.VkCommandBuffer;
import org.lwjgl.vulkan.VkCommandBufferAllocateInfo;
import org.lwjgl.vulkan.VkCommandBufferBeginInfo;
import org.lwjgl.vulkan.VkDescriptorImageInfo;
import org.lwjgl.vulkan.VkDescriptorSetLayoutBinding;
import org.lwjgl.vulkan.VkFramebufferCreateInfo;
import org.lwjgl.vulkan.VkGraphicsPipelineCreateInfo;
import org.lwjgl.vulkan.VkImageCreateInfo;
import org.lwjgl.vulkan.VkImageMemoryBarrier;
import org.lwjgl.vulkan.VkImageViewCreateInfo;
import org.lwjgl.vulkan.VkMemoryAllocateInfo;
import org.lwjgl.vulkan.VkMemoryRequirements;
import org.lwjgl.vulkan.VkPhysicalDeviceMemoryProperties;
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
import org.lwjgl.vulkan.VkRenderPassCreateInfo;
import org.lwjgl.vulkan.VkSamplerCreateInfo;
import org.lwjgl.vulkan.VkSubmitInfo;
import org.lwjgl.vulkan.VkSubpassDependency;
import org.lwjgl.vulkan.VkSubpassDescription;
import org.lwjgl.vulkan.VkViewport;

import api.InputAPI;
import api.vulkan.BufferObject;
import api.vulkan.CommandBuffer;
import api.vulkan.CommandPool;
import api.vulkan.DescriptorLayout;
import api.vulkan.DescriptorPool;
import api.vulkan.DescriptorSet;
import api.vulkan.Device;
import api.vulkan.Framebuffer;
import api.vulkan.Image;
import api.vulkan.Imageview;
import api.vulkan.Instance;
import api.vulkan.PhysicalDevice;
import api.vulkan.GraphicsPipeline;
import api.vulkan.Queue;
import api.vulkan.QueueFamily;
import api.vulkan.QueueSubmitInfo;
import api.vulkan.Renderpass;
import api.vulkan.Shader;
import api.vulkan.Surface;
import api.vulkan.Swapchain;
import api.vulkan.Vertex;
import api.vulkan.Vulkan;
import engine.core.EngineShutdown;
import engine.core.Time;
import engine.debug.Debug;
import engine.objects.World;
import engine.window.Window;
import math.Constants;
import math.Matrix4f;
import math.Quaternion;
import math.Transform3D;
import math.Vector3f;
import math.Vector4f;

public class VulkanRenderingEngine3 extends RenderingEngine {
	Window window;
	Instance instance;
	PhysicalDevice physicalDevice;
	QueueFamily queueFamily;
	Device device;
	Queue graphicsQueue;
	Surface surface;
	Swapchain swapchain;
	Imageview imageview;
	Shader vert, frag;
	Renderpass renderpass;
	GraphicsPipeline pipeline;
	Framebuffer framebuffer;
	CommandPool commandPool;
	CommandBuffer[] commandBuffers;
	CommandBuffer depthPrepassBuffer;
	
	@Override
	public void start() {
		super.start();
		GLFWErrorCallback.createPrint().set();
		initWindow();
		initVK();
	}
	
	public void initWindow() {
		InputAPI.init();
		window = new Window("Vulkan", 800, 600);
		
		InputAPI.setCloseCallback(window.getHandle(), (long window) -> {
			EngineShutdown.shutdown().post();
		});
	}
	
	public void initVK() {
		if(!Vulkan.supported()) Debug.critical("API", "Vulkan not supported!");
		instance = Vulkan.createInstance();
		physicalDevice = instance.getBestPhysicalDevice();
		queueFamily = physicalDevice.getQueueFamilies()[0];
		device = new Device(physicalDevice, queueFamily, new ArrayList<>(), new ArrayList<>());
		graphicsQueue = new Queue(device, queueFamily);
		surface = new Surface(instance, physicalDevice, queueFamily, window);
		vert = new Shader(device, "multilight.vert", Shader.VERTEX_BIT); frag = new Shader(device, "multilight.frag", Shader.FRAGMENT_BIT);
		commandPool = new CommandPool(queueFamily, device);
		
		World.load(physicalDevice, device, commandPool, graphicsQueue, "clustering_test.world");
		Debug.info("API", "World Loaded");
		
		createVertexBuffer();
		Debug.info("API", "Vertex Buffer Created");
		createSwapchain();
		Debug.info("API", "Swap Chain Created");
		
		depthPrepassSemaphore = device.createSemaphore();
		imageAvailableSemaphore = device.createSemaphore();
		renderFinishedSemaphore = device.createSemaphore();
		inFlightFence = device.createFence();
	}
	
	DescriptorLayout desLayout;
	DescriptorPool desPool;
	DescriptorSet desSet;
	DescriptorSet depthSet;
	BufferObject viewProjectionBuffer, dLightBuffer, pLightBuffer;
	BufferObject modelBuffer, materialBuffer, materialIndicesBuffer, textureIndicesBuffer;
	Camera mainCamera;
	
	int numDescriptors = 96;
	int numObjects = 16;
	
	Matrix4f.Buffer transformBuffer;
	
	public void updateTransforms() {
		try(MemoryStack stack = MemoryStack.stackPush()) {
			Transform3D[] transforms = RenderStructure.transforms;
			int numTransforms = transforms.length;
			Matrix4f.Buffer transformBuffer = Matrix4f.mallocStack(numTransforms, stack);
			
			for(int i = 0; i < numTransforms; i++) {
				transformBuffer.put(transforms[i].transform());
			}
			transformBuffer.flip();
			
			modelBuffer.map(transformBuffer);
			
			//System.out.println("Transform buffer updated!");
		}
	}
	
	public void initDepthPrepass() {
		try(MemoryStack stack = MemoryStack.stackPush()) {
			VkAttachmentDescription.Buffer attachments = VkAttachmentDescription.callocStack(1, stack)
			.samples(VK_SAMPLE_COUNT_1_BIT)
			.loadOp(VK_ATTACHMENT_LOAD_OP_CLEAR)
			.storeOp(VK_ATTACHMENT_STORE_OP_STORE)
			.stencilLoadOp(VK_ATTACHMENT_LOAD_OP_DONT_CARE)
			.stencilStoreOp(VK_ATTACHMENT_STORE_OP_DONT_CARE)
			.initialLayout(VK_IMAGE_LAYOUT_DEPTH_STENCIL_ATTACHMENT_OPTIMAL)
			.finalLayout(VK10.VK_IMAGE_LAYOUT_DEPTH_STENCIL_READ_ONLY_OPTIMAL)
			.format(VK10.VK_FORMAT_D32_SFLOAT_S8_UINT);
			
			VkAttachmentReference ref = VkAttachmentReference.callocStack(stack)
			.attachment(0)
			.layout(VK_IMAGE_LAYOUT_DEPTH_STENCIL_ATTACHMENT_OPTIMAL);
			
			VkSubpassDescription.Buffer subpasses = VkSubpassDescription.callocStack(1, stack)
			.pipelineBindPoint(VK_PIPELINE_BIND_POINT_GRAPHICS)
			.colorAttachmentCount(0)
			.pDepthStencilAttachment(ref);
			
			//Might have issues
			VkSubpassDependency.Buffer dependency = VkSubpassDependency.callocStack(1, stack)
			.srcSubpass(VK_SUBPASS_EXTERNAL)
			.dstSubpass(0)
			.srcStageMask(VK_PIPELINE_STAGE_BOTTOM_OF_PIPE_BIT)
			.srcAccessMask(VK_ACCESS_MEMORY_READ_BIT)
			.dstStageMask(VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT)
			.dstAccessMask(VK_ACCESS_COLOR_ATTACHMENT_READ_BIT | VK_ACCESS_COLOR_ATTACHMENT_WRITE_BIT);
			
			VkRenderPassCreateInfo info = VkRenderPassCreateInfo.callocStack(stack)
			.sType(VK_STRUCTURE_TYPE_RENDER_PASS_CREATE_INFO)
			.pAttachments(attachments)
			.pSubpasses(subpasses)
			.pDependencies(dependency);
			
			LongBuffer lb = stack.mallocLong(1);
			if(VK10.vkCreateRenderPass(device.device, info, null, lb) != VK10.VK_SUCCESS)
				Debug.error("API", "Vulkan RenderPass failed to create!");
			else Debug.info("API", "RenderPass created");
			depthRenderpass = lb.get(0);
			
			Shader depthPrepassShader = new Shader(device, "depth prepass.vert", Shader.VERTEX_BIT);
			
			VkPipelineDepthStencilStateCreateInfo depthInfo = VkPipelineDepthStencilStateCreateInfo.callocStack(stack)
			.sType(VK10.VK_STRUCTURE_TYPE_PIPELINE_DEPTH_STENCIL_STATE_CREATE_INFO)
			.depthTestEnable(true)
			.depthWriteEnable(true)
			.depthCompareOp(VK10.VK_COMPARE_OP_LESS)
			.depthBoundsTestEnable(false)
			.minDepthBounds(0.0f)
			.maxDepthBounds(1.0f)
			.stencilTestEnable(false);
			
			//Pipeline shader stages setup
			ByteBuffer main = stack.ASCII("main");
			VkPipelineShaderStageCreateInfo.Buffer stages = VkPipelineShaderStageCreateInfo.callocStack(1, stack)
			.sType(VK_STRUCTURE_TYPE_PIPELINE_SHADER_STAGE_CREATE_INFO)
			.stage(depthPrepassShader.stage)
			.module(depthPrepassShader.module)
			.pName(main);
			
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
			
			VkPipelineLayoutCreateInfo pipelineLayoutInfo = VkPipelineLayoutCreateInfo.callocStack(stack)
			.sType(VK_STRUCTURE_TYPE_PIPELINE_LAYOUT_CREATE_INFO)
			.pSetLayouts(stack.longs(desLayout.layout));
			
			vkCreatePipelineLayout(device.device, pipelineLayoutInfo, null, lb);
			depthPipelineLayout = lb.get(0);
			
			VkGraphicsPipelineCreateInfo.Buffer pipelineInfo = VkGraphicsPipelineCreateInfo.callocStack(1, stack)
			.sType(VK_STRUCTURE_TYPE_GRAPHICS_PIPELINE_CREATE_INFO)
			.pStages(stages)
			.pVertexInputState(vertexInput)
			.pInputAssemblyState(assemblyState)
			.pViewportState(viewportState)
			.pRasterizationState(rasterizer)
			.pMultisampleState(multisampling)
			.pColorBlendState(null)
			.pDepthStencilState(depthInfo)
			.layout(depthPipelineLayout)
			.renderPass(depthRenderpass)
			.subpass(0);
			
			VK10.vkCreateGraphicsPipelines(device.device, MemoryUtil.NULL, pipelineInfo, null, lb);
			depthPipeline = lb.get(0);
			
			VkFramebufferCreateInfo framebufferinfo = VkFramebufferCreateInfo.callocStack(stack)
			.sType(VK_STRUCTURE_TYPE_FRAMEBUFFER_CREATE_INFO)
			.renderPass(depthRenderpass)
			.width(swapchain.extent.width())
			.height(swapchain.extent.height())
			.layers(1)
			.pAttachments(stack.longs(imageView));
				
			if(vkCreateFramebuffer(device.device, framebufferinfo, null, lb) != VK_SUCCESS) System.err.println("Failed to create depth framebuffer");
			depthFramebuffer = lb.get(0);
			
		}
	}
	
	long depthRenderpass, depthPipeline, depthPipelineLayout, depthFramebuffer;
	DescriptorLayout depthLayout;
	
	public void createVertexBuffer() {
		mainCamera = new Camera();
		mainCamera.setPerspective(90f, 800f, 600f, 0.1f, 90f);
		
		desLayout = new DescriptorLayout(device, 2, 5, true);
		depthLayout = new DescriptorLayout(device, 2);
		desPool = new DescriptorPool(device, numDescriptors);

		desSet = new DescriptorSet(device, desPool, desLayout);
		depthSet = new DescriptorSet(device, desPool, depthLayout);
		
		transformBuffer = Matrix4f.calloc(numObjects);
		viewProjectionBuffer = new BufferObject(physicalDevice, device);
		viewProjectionBuffer.createUniformBuffer(16 * 4 * 2);
		
		modelBuffer = new BufferObject(physicalDevice, device);
		modelBuffer.createUniformBuffer(numObjects * 16 * 4);
		
		materialBuffer = new BufferObject(physicalDevice, device);
		materialBuffer.createUniformBuffer(numObjects * 4 * 4 * 4);
		
		dLightBuffer = new BufferObject(physicalDevice, device);
		dLightBuffer.createUniformBuffer(2 * 4 * 4);
		
		pLightBuffer = new BufferObject(physicalDevice, device);
		pLightBuffer.createUniformBuffer(2 * 2 * 4 * 4);
		
		materialIndicesBuffer = new BufferObject(physicalDevice, device);
		materialIndicesBuffer.createUniformBuffer(numObjects * 16);
		
		textureIndicesBuffer = new BufferObject(physicalDevice, device);
		textureIndicesBuffer.createUniformBuffer(numObjects * 16);
		
		Debug.info("API", "Buffers created!");
		
		desSet.bindBuffer(viewProjectionBuffer, 0);
		desSet.bindBuffer(modelBuffer, 1);
		desSet.bindBuffer(materialBuffer, 2);
		desSet.bindBuffer(dLightBuffer, 3);
		desSet.bindBuffer(pLightBuffer, 4);
		desSet.bindBuffer(materialIndicesBuffer, 5);
		desSet.bindBuffer(textureIndicesBuffer, 6);
		
		depthSet.bindBuffer(viewProjectionBuffer, 0);
		depthSet.bindBuffer(modelBuffer, 1);
		
		Debug.info("API", "Buffers bound!");
		
		try(MemoryStack stack = MemoryStack.stackPush()) {
			DirectionalLight dlight = new DirectionalLight();
			dlight.dir.set(0, 0, 1);
			dlight.color.set(1, 0, 0);
			
			PointLight plight0 = new PointLight();
			plight0.pos.set(10, 0, 0);
			plight0.color.set(0, 1, 0);
			
			PointLight plight1 = new PointLight();
			plight1.pos.set(0, 10, 0);
			plight1.color.set(0, 0, 1);
			
			FloatBuffer buffer = stack.mallocFloat(2 * 4);
			buffer.put(dlight.dir.x()); buffer.put(dlight.dir.y()); buffer.put(dlight.dir.z()); buffer.put(0f);
			buffer.put(dlight.color.x()); buffer.put(dlight.color.y()); buffer.put(dlight.color.z()); buffer.put(0f);
			buffer.flip();
			
			dLightBuffer.map(MemoryUtil.memAddress(buffer), 2 * 4 * 4);
			
			buffer = stack.mallocFloat(2 * 2 * 4);
			buffer.put(plight0.pos.x()); buffer.put(plight0.pos.y()); buffer.put(plight0.pos.z()); buffer.put(0f);
			buffer.put(plight0.color.x()); buffer.put(plight0.color.y()); buffer.put(plight0.color.z()); buffer.put(0f);
			buffer.put(plight1.pos.x()); buffer.put(plight1.pos.y()); buffer.put(plight1.pos.z()); buffer.put(0f);
			buffer.put(plight1.color.x()); buffer.put(plight1.color.y()); buffer.put(plight1.color.z()); buffer.put(0f);
			buffer.flip();
			
			pLightBuffer.map(buffer);
			
			Debug.info("API", "Lighting mapped!");
		}
		
		int format = VK10.VK_FORMAT_D32_SFLOAT_S8_UINT;
		image = Image.createImage(physicalDevice, device, 800, 600, format);
		imageMemory = Image.allocateImage(physicalDevice, device, image, VK10.VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT);
		imageView = Image.createImageView(device, image, format);
		Image.transition(commandPool.createBuffer(), graphicsQueue, image, format, VK10.VK_IMAGE_LAYOUT_UNDEFINED, VK10.VK_IMAGE_LAYOUT_DEPTH_STENCIL_ATTACHMENT_OPTIMAL, 0, 0);
		Debug.info("API", "Depth Buffer set up!");
		
		
		sampler = Image.createSampler(device);
		desSet.bindSampler(sampler, 7);
		Debug.info("API", "Sampler bound!");
	}
	
	long image, imageMemory, imageView, sampler;
	
	public void recordCommandBuffer() {
		try(MemoryStack stack = MemoryStack.stackPush()) {
		commandBuffers = commandPool.createBuffer(framebuffer.length());
		RenderStructure.compile();
		
		Model[] models = RenderStructure.models;
		int[] modelSegments = RenderStructure.modelSegments;
		int numModelsM1 = models.length - 1;
		
		Transform3D[] transforms = RenderStructure.transforms;
		int numTransforms = transforms.length;
		Matrix4f.Buffer transformBuffer = Matrix4f.mallocStack(numTransforms, stack);
		
		for(int i = 0; i < numTransforms; i++) {
			transformBuffer.put(transforms[i].transform());
		}
		transformBuffer.flip();
		
		modelBuffer.map(transformBuffer);
		
		Material[] materials = RenderStructure.materials;
		int numMaterials = materials.length;
		Vector4f.Buffer materialsBuffer = Vector4f.mallocStack(numMaterials * 4, stack);
		
		for(int i = 0; i < numMaterials; i++) {
			materialsBuffer.put(materials[i].color);
		}
		materialsBuffer.flip();
		
		materialBuffer.map(materialsBuffer);
		desSet.bindTextures(RenderStructure.textures, 8);
		
		IntBuffer matBuffer = stack.mallocInt(numObjects * 4);
		int[] matI = RenderStructure.materialIndices;
		for(int i = 0; i < matI.length; i++) {
			matBuffer.put(matI[i]); matBuffer.put(0); matBuffer.put(0); matBuffer.put(0);
		}
		matBuffer.flip();
		
		IntBuffer texBuffer = stack.mallocInt(numObjects * 4);
		int[] texI = RenderStructure.textureIndices;
		for(int i = 0; i < texI.length; i++) {
			texBuffer.put(texI[i]); texBuffer.put(0); texBuffer.put(0); texBuffer.put(0);
		}
		texBuffer.flip();
		
		materialIndicesBuffer.map(matBuffer);
		textureIndicesBuffer.map(texBuffer);
		
		depthPrepassBuffer = commandPool.createBuffer();
		
		CommandBuffer cb = depthPrepassBuffer;
		cb.begin();
		cb.beginRenderPass(depthRenderpass, depthFramebuffer, pipeline.extent.width(), pipeline.extent.height());
		cb.bindPipelineGraphics(depthPipeline);
		
		//Bind Uniforms
		cb.bindUniforms(depthPipelineLayout, stack.longs(desSet.set));
		
		for(int j = 0; j < numModelsM1; j++) {
			cb.pushConstants(depthPipelineLayout, VK10.VK_SHADER_STAGE_ALL, 0, stack.ints(modelSegments[j]));
			
			Model model = models[j];
			
			cb.bindVertexBuffer(model.vertices);
			cb.bindIndexBuffer(model.indices);
			cb.draw(model.length, modelSegments[j + 1] - modelSegments[j]);
		}
		
		cb.pushConstants(depthPipelineLayout, VK10.VK_SHADER_STAGE_ALL, 0, stack.ints(modelSegments[numModelsM1]));
		
		Model model = models[numModelsM1];
		
		cb.bindVertexBuffer(model.vertices);
		cb.bindIndexBuffer(model.indices);
		cb.draw(model.length, RenderStructure.transforms.length - modelSegments[numModelsM1]);
		
		cb.endRenderPass();
		cb.end();
		
		for(int i = 0; i < commandBuffers.length; i++) {
			cb = commandBuffers[i];
			cb.begin();
			cb.beginRenderPass(renderpass, framebuffer.get(i), pipeline.extent.width(), pipeline.extent.height()); //TODO: HAck
			cb.bindPipelineGraphics(pipeline);
			//Number of lights
			cb.pushConstants(pipeline.layout, VK10.VK_SHADER_STAGE_ALL, 4, stack.ints(2));
			
			//Bind Uniforms
			cb.bindUniforms(pipeline.layout, stack.longs(desSet.set));
			
			for(int j = 0; j < numModelsM1; j++) {
				cb.pushConstants(pipeline.layout, VK10.VK_SHADER_STAGE_ALL, 0, stack.ints(modelSegments[j]));
				
				model = models[j];
				
				cb.bindVertexBuffer(model.vertices);
				cb.bindIndexBuffer(model.indices);
				cb.draw(model.length, modelSegments[j + 1] - modelSegments[j]);
			}
			
			cb.pushConstants(pipeline.layout, VK10.VK_SHADER_STAGE_ALL, 0, stack.ints(modelSegments[numModelsM1]));
			
			model = models[numModelsM1];
			
			cb.bindVertexBuffer(model.vertices);
			cb.bindIndexBuffer(model.indices);
			cb.draw(model.length, RenderStructure.transforms.length - modelSegments[numModelsM1]);
			
			cb.endRenderPass();
			cb.end();
		}
		}
	}
	
	public void createSwapchain() {
		swapchain = new Swapchain(device, surface);
		imageview = new Imageview(device, swapchain);
		renderpass = new Renderpass(device, swapchain);
		pipeline = new GraphicsPipeline(device, swapchain, renderpass, desLayout, vert, frag);
		initDepthPrepass();
		framebuffer = new Framebuffer(device, swapchain, imageview, renderpass.renderpass, imageView);
		recordCommandBuffer();
	}
	
	public void disposeSwapchain() {
		commandPool.destroyBuffers(commandBuffers);
		framebuffer.dispose();
		pipeline.dispose();
		renderpass.dispose();
		imageview.dispose();
		swapchain.dispose();
	}
	
	long depthPrepassSemaphore, imageAvailableSemaphore, renderFinishedSemaphore, inFlightFence;
	QueueSubmitInfo info = new QueueSubmitInfo();
	@Override
	public void run() {
		try(MemoryStack stack = MemoryStack.stackPush()) {
			IntBuffer ib = stack.mallocInt(1);
			InputAPI.pollEvents();
			
			mainCamera.update(window);
			if(World.updateSelection(window)) updateTransforms();
			viewProjectionBuffer.map(mainCamera.viewProjection);
			
			long depthPrepassSemaphore = this.depthPrepassSemaphore;
			long imageAvailableSemaphore = this.imageAvailableSemaphore;
			long renderFinishedSemaphore = this.renderFinishedSemaphore;
			long inFlightFence = this.inFlightFence;
			
			VK10.vkWaitForFences(device.device, inFlightFence, true, Long.MAX_VALUE);
			
			int result = KHRSwapchain.vkAcquireNextImageKHR(device.device, swapchain.swapchain, 0xFFFFFFFFFFFFFFFFL, imageAvailableSemaphore, 0, ib);
			if(result != VK10.VK_SUCCESS && result != KHRSwapchain.VK_SUBOPTIMAL_KHR) Debug.critical("API", "Failed to aquire next image!");
			
			int imageIndex = ib.get(0);
			VK10.vkResetFences(device.device, inFlightFence);
			
			boolean prepass = false;
			//Depth pre-pass
			if(prepass) {
				info.pCommandBuffers(stack.pointers(depthPrepassBuffer));
				info.pWaitSemaphores(imageAvailableSemaphore);
				info.pSignalSemaphores(depthPrepassSemaphore);
				graphicsQueue.submit(info, inFlightFence);
			}
			
			//Render Scene
			info.pCommandBuffers(stack.pointers(commandBuffers[imageIndex]));
			info.pWaitSemaphores(depthPrepassSemaphore);
			info.pSignalSemaphores(renderFinishedSemaphore);
			graphicsQueue.submit(info, inFlightFence);
			result = graphicsQueue.present(stack, swapchain.swapchain, renderFinishedSemaphore, imageIndex);
			if(result != VK10.VK_SUCCESS) Debug.critical("API", "Failed to aquire next image!");
		}
	}

	@Override
	public void dispose() {
		device.waitIdle();
		
		device.destroySemaphore(imageAvailableSemaphore);
		device.destroySemaphore(renderFinishedSemaphore);
		device.destroyFence(inFlightFence);
		
		disposeSwapchain();
		
		vert.dispose();
		frag.dispose();
		commandPool.dispose();
		
		surface.dispose();
		device.dispose();
		Vulkan.dispose();
		window.dispose();
		GLFW.glfwTerminate();
		
		super.dispose();
	}
	
}
