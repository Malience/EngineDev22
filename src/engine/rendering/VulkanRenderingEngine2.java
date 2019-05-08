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
import org.lwjgl.vulkan.VkBufferCopy;
import org.lwjgl.vulkan.VkBufferCreateInfo;
import org.lwjgl.vulkan.VkBufferImageCopy;
import org.lwjgl.vulkan.VkCommandBuffer;
import org.lwjgl.vulkan.VkCommandBufferAllocateInfo;
import org.lwjgl.vulkan.VkCommandBufferBeginInfo;
import org.lwjgl.vulkan.VkImageCreateInfo;
import org.lwjgl.vulkan.VkImageMemoryBarrier;
import org.lwjgl.vulkan.VkMemoryAllocateInfo;
import org.lwjgl.vulkan.VkMemoryRequirements;
import org.lwjgl.vulkan.VkPhysicalDeviceMemoryProperties;
import org.lwjgl.vulkan.VkSubmitInfo;

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
import engine.window.Window;
import math.Constants;
import math.Matrix4f;
import math.Quaternion;
import math.Vector3f;
import math.Vector4f;

public class VulkanRenderingEngine2 extends RenderingEngine {
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
		createVertexBuffer();
		createSwapchain();
		
		imageAvailableSemaphore = device.createSemaphore();
		renderFinishedSemaphore = device.createSemaphore();
		inFlightFence = device.createFence();
	}
	
	DescriptorLayout desLayout;
	DescriptorPool desPool;
	DescriptorSet desSet;
	BufferObject viewProjectionBuffer, dLightBuffer, pLightBuffer;
	BufferObject modelBuffer, texturesBuffer;
	Camera mainCamera;
	
	public void createVertexBuffer() {
		int vdiv = 10; int hdiv = 10;
		Vertex[] vertices = Shape.generateSphereVertices(1f, vdiv, hdiv);
		IntBuffer indices = Shape.generateSphereIndices(vdiv, hdiv);
		
		sphere = new Model("sphere");
		
		sphere.vertices = new BufferObject(physicalDevice, device);
		sphere.vertices.createVertexBuffer(vertices.length * Vertex.SIZEOF);
		sphere.vertices.load(graphicsQueue, commandPool, vertices);
		
		sphere.indices = new BufferObject(physicalDevice, device);
		sphere.indices.createIndexBuffer(indices.limit() * 4);
		sphere.indices.load(graphicsQueue, commandPool, indices);
		
		sphere.length = indices.remaining();
		
		viewProjectionBuffer = new BufferObject(physicalDevice, device);
		viewProjectionBuffer.createUniformBuffer(16 * 4 * 2);
		
		dLightBuffer = new BufferObject(physicalDevice, device);
		dLightBuffer.createUniformBuffer(2 * 4 * 4);
		
		pLightBuffer = new BufferObject(physicalDevice, device);
		pLightBuffer.createUniformBuffer(2 * 2 * 4 * 4);
		
		mainCamera = new Camera();
		mainCamera.setPerspective(90f, 800f, 600f, 0.1f, 40f);
		
		desLayout = new DescriptorLayout(device, 2, 3);
		desPool = new DescriptorPool(device, 96);
		
		desSet = new DescriptorSet(device, desPool, desLayout);
		modelBuffer = new BufferObject(physicalDevice, device);
		texturesBuffer = new BufferObject(physicalDevice, device);
		
		Material material = new Material();
		
		try(MemoryStack stack = MemoryStack.stackPush()) {
			Matrix4f model = Matrix4f.callocStack(stack).identity();
			material.setAmbientColor(0.1f, 0.1f, 0.1f, 1f);
			material.setDiffuseColor(0.4f, 0.4f, 0.4f, 1f);
			material.setSpecularColor(0.3f, 0.3f, 0.3f, 1f);
			material.setEmissiveColor(0f, 0f, 0f, 1f);
			
			modelBuffer = new BufferObject(physicalDevice, device);
			modelBuffer.createUniformBuffer(1 * 16 * 4);
			texturesBuffer = new BufferObject(physicalDevice, device);
			texturesBuffer.createUniformBuffer(1 * 4 * 4 * 4);
			desSet = new DescriptorSet(device, desPool, desLayout);
			
			desSet.bindBuffer(viewProjectionBuffer, 0);
			desSet.bindBuffer(modelBuffer, 1);
			desSet.bindBuffer(texturesBuffer, 2);
			desSet.bindBuffer(dLightBuffer, 3);
			desSet.bindBuffer(pLightBuffer, 4);
			
			modelBuffer.map(model);
			texturesBuffer.map(material.color.address(), 4*4*4);
			
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
		}
		
		int format = VK10.VK_FORMAT_D32_SFLOAT_S8_UINT;
		image = Image.createImage(physicalDevice, device, 800, 600, format);
		imageMemory = Image.allocateImage(physicalDevice, device, image, VK10.VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT);
		imageView = Image.createImageView(device, image, format);
		//Image.transition(commandPool.createBuffer(), graphicsQueue, image, format, VK10.VK_IMAGE_LAYOUT_UNDEFINED, VK10.VK_IMAGE_LAYOUT_DEPTH_STENCIL_ATTACHMENT_OPTIMAL);
		
		dragon = Model.load(physicalDevice, device, graphicsQueue, commandPool, "Basic Cube 2.obj");
		
		try(MemoryStack stack = MemoryStack.stackPush()) {
			LongBuffer lb = stack.mallocLong(1);
			IntBuffer width = stack.mallocInt(1), height = stack.mallocInt(1), channels = stack.mallocInt(1);
			ByteBuffer tex = STBImage.stbi_load("./res/heightmap.png", width, height, channels, STBImage.STBI_rgb_alpha);
			int texSize = width.get(0) * height.get(0) * 4;
			//System.out.println(tex.limit());
			//Create Buffer
			VkBufferCreateInfo bufferInfo = VkBufferCreateInfo.callocStack(stack)
			.sType(VK_STRUCTURE_TYPE_BUFFER_CREATE_INFO)
			.size(texSize)
			.usage(VK_BUFFER_USAGE_TRANSFER_SRC_BIT)
			.sharingMode(VK_SHARING_MODE_EXCLUSIVE);
			
			if(vkCreateBuffer(device.device, bufferInfo, null, lb) != VK_SUCCESS)
				Debug.error("API", "Texture Buffer creation failed!");
			long stagingTextureBuffer = lb.get(0);
			
			//Memory Requirements
			VkMemoryRequirements req = VkMemoryRequirements.mallocStack(stack);
			vkGetBufferMemoryRequirements(device.device, stagingTextureBuffer, req);
			//Prop and Allocate
			VkPhysicalDeviceMemoryProperties pdeviceprop = VkPhysicalDeviceMemoryProperties.mallocStack(stack);
			vkGetPhysicalDeviceMemoryProperties(physicalDevice.device, pdeviceprop);
			
			int num = pdeviceprop.memoryTypeCount();
			int filter = req.memoryTypeBits();
			int index = -1;
			for(int i = 0; i < num; i++) {
				if((filter & (1 << i)) != 0 && (pdeviceprop.memoryTypes(i).propertyFlags() & (VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT | VK_MEMORY_PROPERTY_HOST_COHERENT_BIT)) != 0) {
					index = i; break;
				}
			}
			//System.out.println(index);
			VkMemoryAllocateInfo meminfo = VkMemoryAllocateInfo.callocStack(stack)
			.sType(VK_STRUCTURE_TYPE_MEMORY_ALLOCATE_INFO)
			.allocationSize(req.size())
			.memoryTypeIndex(index);
			
			if(vkAllocateMemory(device.device, meminfo, null, lb) != VK_SUCCESS)
				Debug.error("API", "Allocate Memory failed!");
			if(vkBindBufferMemory(device.device, stagingTextureBuffer, lb.get(0), 0) != VK_SUCCESS)
				Debug.error("API", "Bind Buffer Memory failed!");
			long stagingTextureMemory = lb.get(0);
			
			PointerBuffer pb = stack.mallocPointer(1);
			vkMapMemory(device.device, stagingTextureMemory, 0, texSize, 0, pb);
			MemoryUtil.memCopy(MemoryUtil.memAddress(tex), pb.get(0), texSize);
			vkUnmapMemory(device.device, stagingTextureMemory);
			
			STBImage.stbi_image_free(tex);
			
			VkImageCreateInfo imageInfo = VkImageCreateInfo.callocStack(stack)
			.sType(VK_STRUCTURE_TYPE_IMAGE_CREATE_INFO)
			.imageType(VK_IMAGE_TYPE_2D)
			.mipLevels(1)
			.arrayLayers(1)
			.format(VK_FORMAT_R8G8B8A8_UNORM)
			.tiling(VK_IMAGE_TILING_OPTIMAL)
			.initialLayout(VK_IMAGE_LAYOUT_UNDEFINED)
			.usage(VK_IMAGE_USAGE_TRANSFER_DST_BIT | VK_IMAGE_USAGE_SAMPLED_BIT)
			.sharingMode(VK_SHARING_MODE_EXCLUSIVE)
			.samples(VK_SAMPLE_COUNT_1_BIT)
			.flags(0);
			imageInfo.extent().width(width.get(0)).height(height.get(0)).depth(1);
			
			if(vkCreateImage(device.device, imageInfo, null, lb) != VK_SUCCESS)
				Debug.error("API", "Image failed to create!");
			long textureImage = lb.get(0);
			
			//Memory Requirements
			VkMemoryRequirements req2 = VkMemoryRequirements.mallocStack(stack);
			vkGetImageMemoryRequirements(device.device, textureImage, req2);
			//Prop and Allocate
			VkPhysicalDeviceMemoryProperties pdeviceprop2 = VkPhysicalDeviceMemoryProperties.mallocStack(stack);
			vkGetPhysicalDeviceMemoryProperties(physicalDevice.device, pdeviceprop2);
			
			num = pdeviceprop2.memoryTypeCount();
			filter = req2.memoryTypeBits();
			index = -1;
			for(int i = 0; i < num; i++) {
				if((filter & (1 << i)) != 0 && (pdeviceprop2.memoryTypes(i).propertyFlags() & VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT) != 0) {
					index = i; break;
				}
			}
			
			VkMemoryAllocateInfo meminfo2 = VkMemoryAllocateInfo.callocStack(stack)
			.sType(VK_STRUCTURE_TYPE_MEMORY_ALLOCATE_INFO)
			.allocationSize(req.size())
			.memoryTypeIndex(index);
			
			if(vkAllocateMemory(device.device, meminfo2, null, lb) != VK_SUCCESS)
				Debug.error("API", "Allocate Memory failed!");
			long textureMemory = lb.get(0);
			if(vkBindImageMemory(device.device, image, textureMemory, 0L) != VK_SUCCESS)
				Debug.error("API", "Image could not be bound!");
			
			VkCommandBufferAllocateInfo ballocInfo = VkCommandBufferAllocateInfo.callocStack(stack)
			.sType(VK_STRUCTURE_TYPE_COMMAND_BUFFER_ALLOCATE_INFO)
			.level(VK_COMMAND_BUFFER_LEVEL_PRIMARY)
			.commandPool(commandPool.commandPool)
			.commandBufferCount(1);
			
			vkAllocateCommandBuffers(device.device, ballocInfo, pb);
			
			VkCommandBuffer cb = new VkCommandBuffer(pb.get(0), device.device);
			
			VkCommandBufferBeginInfo beginInfo = VkCommandBufferBeginInfo.callocStack(stack)
			.sType(VK_STRUCTURE_TYPE_COMMAND_BUFFER_BEGIN_INFO)
			.flags(VK_COMMAND_BUFFER_USAGE_ONE_TIME_SUBMIT_BIT);
			
			vkBeginCommandBuffer(cb, beginInfo);
			
			VkImageMemoryBarrier.Buffer barrier = VkImageMemoryBarrier.callocStack(1, stack)
			.sType(VK_STRUCTURE_TYPE_IMAGE_MEMORY_BARRIER)
			.oldLayout(VK_IMAGE_LAYOUT_UNDEFINED)
			.newLayout(VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL)
			.srcQueueFamilyIndex(VK_QUEUE_FAMILY_IGNORED)
			.dstQueueFamilyIndex(VK_QUEUE_FAMILY_IGNORED)
			.image(textureImage)
			.srcAccessMask(0)
			.dstAccessMask(VK_ACCESS_TRANSFER_WRITE_BIT);
			
			barrier.subresourceRange()
			.aspectMask(VK_IMAGE_ASPECT_COLOR_BIT)
			.baseMipLevel(0)
			.levelCount(1)
			.baseArrayLayer(0)
			.layerCount(1);
			
			vkCmdPipelineBarrier(cb, VK_PIPELINE_STAGE_TOP_OF_PIPE_BIT, VK_PIPELINE_STAGE_TRANSFER_BIT, 0, null, null, barrier);
			
			
			
			VkBufferImageCopy.Buffer copy = VkBufferImageCopy.callocStack(1, stack)
			.bufferOffset(0)
			.bufferRowLength(0)
			.bufferImageHeight(0);
			
			copy.imageSubresource()
			.aspectMask(VK_IMAGE_ASPECT_COLOR_BIT)
			.mipLevel(0)
			.baseArrayLayer(0)
			.layerCount(1);
			
			copy.imageOffset().set(0, 0, 0);
			copy.imageExtent().set(width.get(0), height.get(0), 1);
			
			vkCmdCopyBufferToImage(cb, stagingTextureBuffer, textureImage, VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL, copy);
			
			barrier.oldLayout(VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL)
			.newLayout(VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL)
			.srcAccessMask(VK_ACCESS_TRANSFER_WRITE_BIT)
			.dstAccessMask(VK_ACCESS_SHADER_READ_BIT);
			
			vkCmdPipelineBarrier(cb, VK_PIPELINE_STAGE_TRANSFER_BIT, VK_PIPELINE_STAGE_FRAGMENT_SHADER_BIT, 0, null, null, barrier);
			
			vkEndCommandBuffer(cb);
			
			VkSubmitInfo submitInfo = VkSubmitInfo.callocStack(stack)
			.sType(VK_STRUCTURE_TYPE_SUBMIT_INFO)
			.pCommandBuffers(pb);
			
			vkQueueSubmit(graphicsQueue.queue, submitInfo, VK_NULL_HANDLE);
			vkQueueWaitIdle(graphicsQueue.queue);
			
			vkFreeCommandBuffers(device.device, commandPool.commandPool, pb);
			
		}
		
		
	}
	Model sphere, dragon;
	long image, imageMemory, imageView;
	
	public void recordCommandBuffer() {
		try(MemoryStack stack = MemoryStack.stackPush()) {
		commandBuffers = commandPool.createBuffer(framebuffer.length());
		for(int i = 0; i < commandBuffers.length; i++) {
			CommandBuffer cb = commandBuffers[i];
			cb.begin();
			cb.beginRenderPass(renderpass, framebuffer.get(i), pipeline.extent.width(), pipeline.extent.height()); //TODO: HAck
			cb.bindPipelineGraphics(pipeline);
			cb.pushConstants(pipeline.layout, VK10.VK_SHADER_STAGE_ALL, 0, stack.ints(0));
			cb.pushConstants(pipeline.layout, VK10.VK_SHADER_STAGE_ALL, 4, stack.ints(2));
			cb.bindUniforms(pipeline.layout, stack.longs(desSet.set));
			
			cb.bindVertexBuffer(sphere.vertices);
			cb.bindIndexBuffer(sphere.indices);
			cb.draw(sphere.length, 96);
			
//			cb.bindVertexBuffer(dragon.vertices);
//			cb.bindIndexBuffer(dragon.indices);
//			cb.draw(dragon.length, 96);
			
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
		framebuffer = new Framebuffer(device, swapchain, imageview, renderpass, imageView);
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
	
	long imageAvailableSemaphore, renderFinishedSemaphore, inFlightFence;
	QueueSubmitInfo info = new QueueSubmitInfo();
	@Override
	public void run() {
		try(MemoryStack stack = MemoryStack.stackPush()) {
			IntBuffer ib = stack.mallocInt(1);
			InputAPI.pollEvents();
			mainCamera.update(window);
			viewProjectionBuffer.map(mainCamera.viewProjection);
			
			long imageAvailableSemaphore = this.imageAvailableSemaphore;
			long renderFinishedSemaphore = this.renderFinishedSemaphore;
			long inFlightFence = this.inFlightFence;
			
			VK10.vkWaitForFences(device.device, inFlightFence, true, Long.MAX_VALUE);
			
			int result = KHRSwapchain.vkAcquireNextImageKHR(device.device, swapchain.swapchain, 0xFFFFFFFFFFFFFFFFL, imageAvailableSemaphore, 0, ib);
			if(result != VK10.VK_SUCCESS && result != KHRSwapchain.VK_SUBOPTIMAL_KHR) Debug.critical("API", "Failed to aquire next image!");
			
			int imageIndex = ib.get(0);
			VK10.vkResetFences(device.device, inFlightFence);
			
			info.pCommandBuffers(stack.pointers(commandBuffers[imageIndex]));
			info.pWaitSemaphores(imageAvailableSemaphore);
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
		
		sphere.dispose();
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
