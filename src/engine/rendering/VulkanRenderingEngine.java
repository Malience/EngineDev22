package engine.rendering;

import static org.lwjgl.vulkan.VK10.VK_BUFFER_USAGE_TRANSFER_DST_BIT;
import static org.lwjgl.vulkan.VK10.VK_BUFFER_USAGE_TRANSFER_SRC_BIT;
import static org.lwjgl.vulkan.VK10.VK_BUFFER_USAGE_VERTEX_BUFFER_BIT;
import static org.lwjgl.vulkan.VK10.VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT;
import static org.lwjgl.vulkan.VK10.VK_MEMORY_PROPERTY_HOST_COHERENT_BIT;
import static org.lwjgl.vulkan.VK10.VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT;
import static org.lwjgl.vulkan.VK10.VK_SHARING_MODE_EXCLUSIVE;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_BUFFER_CREATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_MEMORY_ALLOCATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_SUCCESS;
import static org.lwjgl.vulkan.VK10.vkAllocateMemory;
import static org.lwjgl.vulkan.VK10.vkBindBufferMemory;
import static org.lwjgl.vulkan.VK10.vkCreateBuffer;
import static org.lwjgl.vulkan.VK10.vkDestroyBuffer;
import static org.lwjgl.vulkan.VK10.vkDeviceWaitIdle;
import static org.lwjgl.vulkan.VK10.vkFreeMemory;
import static org.lwjgl.vulkan.VK10.vkGetBufferMemoryRequirements;
import static org.lwjgl.vulkan.VK10.vkGetPhysicalDeviceMemoryProperties;
import static org.lwjgl.vulkan.VK10.vkMapMemory;
import static org.lwjgl.vulkan.VK10.vkUnmapMemory;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.util.ArrayList;

import org.lwjgl.PointerBuffer;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.stb.STBIEOFCallback;
import org.lwjgl.stb.STBImage;
import org.lwjgl.system.MathUtil;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.vulkan.KHRSwapchain;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkBufferCreateInfo;
import org.lwjgl.vulkan.VkDescriptorBufferInfo;
import org.lwjgl.vulkan.VkDescriptorPoolCreateInfo;
import org.lwjgl.vulkan.VkDescriptorPoolSize;
import org.lwjgl.vulkan.VkDescriptorSetAllocateInfo;
import org.lwjgl.vulkan.VkImageCreateInfo;
import org.lwjgl.vulkan.VkImageMemoryRequirementsInfo2;
import org.lwjgl.vulkan.VkMemoryAllocateInfo;
import org.lwjgl.vulkan.VkMemoryRequirements;
import org.lwjgl.vulkan.VkPhysicalDeviceMemoryProperties;
import org.lwjgl.vulkan.VkWriteDescriptorSet;

import api.InputAPI;
import api.vulkan.CommandBuffer;
import api.vulkan.CommandPool;
import api.vulkan.Device;
import api.vulkan.Framebuffer;
import api.vulkan.HotSwap;
import api.vulkan.Imageview;
import api.vulkan.Instance;
import api.vulkan.PhysicalDevice;
import api.vulkan.Pipeline;
import api.vulkan.Queue;
import api.vulkan.QueueFamily;
import api.vulkan.QueueSubmitInfo;
import api.vulkan.Renderpass;
import api.vulkan.Shader;
import api.vulkan.Surface;
import api.vulkan.Swapchain;
import api.vulkan.Vertex;
import api.vulkan.BufferObject;
import api.vulkan.Vulkan;
import engine.core.EngineShutdown;
import engine.core.Time;
import engine.debug.Debug;
import engine.input.Input;
import engine.window.Window;
import math.Constants;
import math.Matrix4f;
import math.Quaternion;
import math.Sphere;
import math.Vector3f;

public class VulkanRenderingEngine extends RenderingEngine {
	IntBuffer intbuffer;
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
	Pipeline pipeline;
	Framebuffer framebuffer;
	CommandPool commandPool;
	CommandBuffer[] commandBuffers;
	
	@Override
	public void start() {
		super.start();
		intbuffer = MemoryUtil.memAllocInt(20);
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
		
		org.lwjgl.glfw.GLFW.glfwSetFramebufferSizeCallback(window.getHandle(), (long window, int width, int height) -> {
			framebufferResized = true;
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
		vert = new Shader(device, "tri.vert", Shader.VERTEX_BIT); frag = new Shader(device, "tri.frag", Shader.FRAGMENT_BIT);
		commandPool = new CommandPool(queueFamily, device);
		createVertexBuffer();
		createSwapchain();
		
		imageAvailableSemaphore = device.createSemaphore(MAX_FRAMES);
		renderFinishedSemaphore = device.createSemaphore(MAX_FRAMES);
		inFlightFence = device.createFence(MAX_FRAMES);
	}
	
	Vertex[] vertices;
	IntBuffer indices;
	
	public void createVertexBuffer() {
		int vdiv = 20; int hdiv = 20;
		vertices = Sphere.generateSphereVertices(1, vdiv, hdiv);
		indices = Sphere.generateSphereIndices(vdiv, hdiv);
//		vertices = new Vertex[4];
//		vertices[0] = new Vertex(-.5f, -.5f, 0, 1, 0, 0);
//		vertices[1] = new Vertex(.5f, -.5f, 0, 0, 1, 0);
//		vertices[2] = new Vertex(.5f, .5f, 0, 0, 0, 1);
//		vertices[3] = new Vertex(-.5f, .5f, 0, 1, 1, 1);
		
//		float sq22 = (float)Math.sqrt(2)/2;
//		
//		vertices = new Vertex[5];
//		
//		vertices[0] = new Vertex(0, 1, 0, 1, 0, 0);
//		vertices[1] = new Vertex(1, 0, 0, 0, 1, 0);
//		vertices[2] = new Vertex(-sq22, 0, sq22, 0, 0, 1);
//		vertices[3] = new Vertex(-sq22, 0, -sq22, 1, 1, 0);
//		vertices[4] = new Vertex(0,-1, 0, 1, 1, 1);
//		
//		indices.put(new int[]{
//				0, 1, 2, 
//				1, 4, 2,
//				0, 2, 3, 
//				2, 4, 3, 
//				0, 3, 1,
//				4, 1, 3,
//		});
//		//indices.put(new int[]{0, 3, 1});
//		indices.flip();
		
//		indices.put(new int[]{0, 1, 2, 2, 3, 0});
//		indices.flip();
		//long stagingbuffer = createBuffer(vertices, VK10.VK_BUFFER_USAGE_VERTEX_BUFFER_BIT, VK10.VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT | VK10.VK_MEMORY_PROPERTY_HOST_COHERENT_BIT);
		//vb = new VertexBuffer(physicalDevice, device, vertices, VK10.VK_BUFFER_USAGE_TRANSFER_DST_BIT | VK10.VK_BUFFER_USAGE_VERTEX_BUFFER_BIT, VK10.VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT);
		vertexBuffer = new BufferObject(physicalDevice, device);
		vertexBuffer.createVertexBuffer(vertices.length * Vertex.SIZEOF);
		vertexBuffer.load(graphicsQueue, commandPool, vertices);
//		vertexBuffer.load(graphicsQueue, commandPool, indices);
//		
//		vertexBuffer.load(graphicsQueue, commandPool, vertices);
		
		indexBuffer = new BufferObject(physicalDevice, device);
		indexBuffer.createIndexBuffer(indices.limit() * 4);
		indexBuffer.load(graphicsQueue, commandPool, indices);
		
		int uniformSize = Matrix4f.SIZEOF * 3;
		cameraUniform = new BufferObject(physicalDevice, device);
		cameraUniform.createUniformBuffer(uniformSize);
		
		texturesUniform = new BufferObject(physicalDevice, device);
		texturesUniform.createUniformBuffer(4 * 4 * 4);
		
		lightUniform = new BufferObject(physicalDevice, device);
		lightUniform.createUniformBuffer(4 * 2 * 4);

		try(MemoryStack stack = MemoryStack.stackPush()) {
			LongBuffer lb = stack.mallocLong(1);
			VkDescriptorPoolSize.Buffer poolSize = VkDescriptorPoolSize.callocStack(1, stack)
			.type(VK10.VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER)
			.descriptorCount(1);
			
			VkDescriptorPoolCreateInfo poolInfo = VkDescriptorPoolCreateInfo.callocStack(stack)
			.sType(VK10.VK_OBJECT_TYPE_DESCRIPTOR_POOL)
			.pPoolSizes(poolSize)
			.maxSets(3);
			
			VK10.vkCreateDescriptorPool(device.device, poolInfo, null, lb);
			descriptorPool = lb.get(0);
//			IntBuffer width = stack.mallocInt(1), height = stack.mallocInt(1), channels = stack.mallocInt(1);
//			ByteBuffer tex = STBImage.stbi_load("./res/textures/mud.png", width, height, channels, STBImage.STBI_rgb_alpha);
//			int texSize = width.get(0) * height.get(0) * 4;
//			//System.out.println(tex.limit());
//			//Create Buffer
//			VkBufferCreateInfo bufferInfo = VkBufferCreateInfo.callocStack(stack)
//			.sType(VK_STRUCTURE_TYPE_BUFFER_CREATE_INFO)
//			.size(texSize)
//			.usage(VK_BUFFER_USAGE_TRANSFER_SRC_BIT)
//			.sharingMode(VK_SHARING_MODE_EXCLUSIVE);
//			
//			if(vkCreateBuffer(device.device, bufferInfo, null, lb) != VK_SUCCESS)
//				Debug.error("API", "Texture Buffer creation failed!");
//			texBuffer = lb.get(0);
//			
//			//Memory Requirements
//			VkMemoryRequirements req = VkMemoryRequirements.mallocStack(stack);
//			vkGetBufferMemoryRequirements(device.device, texBuffer, req);
//			//Prop and Allocate
//			VkPhysicalDeviceMemoryProperties pdeviceprop = VkPhysicalDeviceMemoryProperties.mallocStack(stack);
//			vkGetPhysicalDeviceMemoryProperties(physicalDevice.device, pdeviceprop);
//			
//			int num = pdeviceprop.memoryTypeCount();
//			int filter = req.memoryTypeBits();
//			int index = -1;
//			for(int i = 0; i < num; i++) {
//				if((filter & (1 << i)) != 0 && (pdeviceprop.memoryTypes(i).propertyFlags() & (VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT | VK_MEMORY_PROPERTY_HOST_COHERENT_BIT)) != 0) {
//					index = i; break;
//				}
//			}
//			//System.out.println(index);
//			VkMemoryAllocateInfo meminfo = VkMemoryAllocateInfo.callocStack(stack)
//			.sType(VK_STRUCTURE_TYPE_MEMORY_ALLOCATE_INFO)
//			.allocationSize(req.size())
//			.memoryTypeIndex(index);
//			
//			if(vkAllocateMemory(device.device, meminfo, null, lb) != VK_SUCCESS)
//				Debug.error("API", "Allocate Memory failed!");
//			if(vkBindBufferMemory(device.device, texBuffer, lb.get(0), 0) != VK_SUCCESS)
//				Debug.error("API", "Bind Buffer Memory failed!");
//			texMemory = lb.get(0);
//			
//			PointerBuffer pb = stack.mallocPointer(1);
//			vkMapMemory(device.device, texMemory, 0, texSize, 0, pb);
//			MemoryUtil.memCopy(MemoryUtil.memAddress(tex), pb.get(0), texSize);
//			vkUnmapMemory(device.device, texMemory);
//			
//			STBImage.stbi_image_free(tex);
//			
//			VkImageCreateInfo imageInfo = VkImageCreateInfo.callocStack(stack)
//			.sType(VK10.VK_STRUCTURE_TYPE_IMAGE_CREATE_INFO)
//			.imageType(VK10.VK_IMAGE_TYPE_2D)
//			.mipLevels(1)
//			.arrayLayers(1)
//			.format(VK10.VK_FORMAT_R8G8B8A8_UNORM)
//			.tiling(VK10.VK_IMAGE_TILING_OPTIMAL)
//			.initialLayout(VK10.VK_IMAGE_LAYOUT_UNDEFINED)
//			.usage(VK10.VK_IMAGE_USAGE_TRANSFER_DST_BIT | VK10.VK_IMAGE_USAGE_SAMPLED_BIT)
//			.sharingMode(VK10.VK_SHARING_MODE_EXCLUSIVE)
//			.samples(VK10.VK_SAMPLE_COUNT_1_BIT)
//			.flags(0);
//			imageInfo.extent().width(width.get(0)).height(height.get(0)).depth(1);
//			
//			if(VK10.vkCreateImage(device.device, imageInfo, null, lb) != VK_SUCCESS)
//				Debug.error("API", "Image failed to create!");
//			texture = lb.get(0);
//			
//			//Memory Requirements
//			VK10.vkGetImageMemoryRequirements(device.device, texture, req);
//			//Prop and Allocate
//			//VkPhysicalDeviceMemoryProperties pdeviceprop = VkPhysicalDeviceMemoryProperties.mallocStack(stack);
//			//vkGetPhysicalDeviceMemoryProperties(physicalDevice.device, pdeviceprop);
//			
//			//int num = pdeviceprop.memoryTypeCount();
//			filter = req.memoryTypeBits();
//			index = -1;
//			for(int i = 0; i < num; i++) {
//				if((filter & (1 << i)) != 0 && (pdeviceprop.memoryTypes(i).propertyFlags() & VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT) != 0) {
//					index = i; break;
//				}
//			}
//			
//			meminfo.allocationSize(req.size())
//			.memoryTypeIndex(index);
//			
//			if(vkAllocateMemory(device.device, meminfo, null, lb) != VK_SUCCESS)
//				Debug.error("API", "Allocate Memory failed!");
//			textureMemory = lb.get(0);
//			VK10.vkBindImageMemory(device.device, texture, textureMemory, 0L);
		}
	}
	long texBuffer, texMemory, texture, textureMemory;
	long descriptorPool, cameraSet, texturesSet, lightSet;
	BufferObject vertexBuffer, indexBuffer, cameraUniform, texturesUniform, lightUniform;
	
	public void recordCommandBuffer() {
		try(MemoryStack stack = MemoryStack.stackPush()) {
		commandBuffers = commandPool.createBuffer(framebuffer.length());
		for(int i = 0; i < commandBuffers.length; i++) {
			CommandBuffer cb = commandBuffers[i];
			cb.begin();
			cb.beginRenderPass(renderpass, framebuffer.get(i), pipeline.extent.width(), pipeline.extent.height()); //TODO: HAck
			cb.bindPipelineGraphics(pipeline);
			cb.bindVertexBuffer(vertexBuffer);
			cb.bindIndexBuffer(indexBuffer);
			//cb.draw(3, 1, 0, 0);
			cb.bindUniforms(pipeline.layout, stack.longs(cameraSet));//, texturesSet, lightSet));
			//cb.updateUniforms(pipeline.layout, texturesSet);
			//cb.updateUniforms(pipeline.layout, lightSet);
			cb.draw(indices.limit());
			cb.endRenderPass();
			cb.end();
		}
		}
	}
	
	public void createSwapchain() {
		swapchain = new Swapchain(device, surface);
		imageview = new Imageview(device, swapchain);
		renderpass = new Renderpass(device, swapchain);
		pipeline = new Pipeline(device, swapchain, renderpass, vert, frag);
		try(MemoryStack stack = MemoryStack.stackPush()) {
			LongBuffer lb = stack.mallocLong(3);
		VkDescriptorSetAllocateInfo setAllocateInfo = VkDescriptorSetAllocateInfo.callocStack(stack)
		.sType(VK10.VK_STRUCTURE_TYPE_DESCRIPTOR_SET_ALLOCATE_INFO)
		.descriptorPool(descriptorPool)
		.pSetLayouts(stack.longs(pipeline.cameraLayout));//, pipeline.texturesLayout, pipeline.lightLayout));
		
		int result = VK10.vkAllocateDescriptorSets(device.device, setAllocateInfo, lb);
		if(result != VK_SUCCESS) {
			Debug.error("API", "Failed to Allocate Descriptor Sets!");
			System.out.println(result);
		}
		cameraSet = lb.get(0);
		texturesSet = lb.get(1);
		lightSet = lb.get(2);
		
		VkDescriptorBufferInfo.Buffer desBufferInfo0 = VkDescriptorBufferInfo.callocStack(1, stack)
		.buffer(cameraUniform.getBuffer())
		.offset(0L)
		.range(VK10.VK_WHOLE_SIZE);
		VkDescriptorBufferInfo.Buffer desBufferInfo1 = VkDescriptorBufferInfo.callocStack(1, stack)
		.buffer(texturesUniform.getBuffer())
		.offset(0L)
		.range(VK10.VK_WHOLE_SIZE);
		VkDescriptorBufferInfo.Buffer desBufferInfo2 = VkDescriptorBufferInfo.callocStack(1, stack)
		.buffer(lightUniform.getBuffer())
		.offset(0L)
		.range(VK10.VK_WHOLE_SIZE);
		
		VkWriteDescriptorSet.Buffer desWrite = VkWriteDescriptorSet.callocStack(3, stack)
		.sType(VK10.VK_STRUCTURE_TYPE_WRITE_DESCRIPTOR_SET)
		.dstSet(cameraSet)
		.dstBinding(0)
		.dstArrayElement(0)
		.descriptorType(VK10.VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER)
		.pBufferInfo(desBufferInfo0)
		.pImageInfo(null)
		.pTexelBufferView(null);
		desWrite.get(1)
		.sType(VK10.VK_STRUCTURE_TYPE_WRITE_DESCRIPTOR_SET)
		.dstSet(cameraSet)
		.dstBinding(1)
		.dstArrayElement(0)
		.descriptorType(VK10.VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER)
		.pBufferInfo(desBufferInfo1)
		.pImageInfo(null)
		.pTexelBufferView(null);
		desWrite.get(2)
		.sType(VK10.VK_STRUCTURE_TYPE_WRITE_DESCRIPTOR_SET)
		.dstSet(cameraSet)
		.dstBinding(2)
		.dstArrayElement(0)
		.descriptorType(VK10.VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER)
		.pBufferInfo(desBufferInfo2)
		.pImageInfo(null)
		.pTexelBufferView(null);
		
		VK10.vkUpdateDescriptorSets(device.device, desWrite, null);
		
		
		
		
		framebuffer = new Framebuffer(device, swapchain, imageview, renderpass);
		recordCommandBuffer();
		}
	}
	
	public void createSwapchain(IntBuffer width, IntBuffer height) {
		swapchain = new Swapchain(device, surface, width, height);
		imageview = new Imageview(device, swapchain);
		renderpass = new Renderpass(device, swapchain);
		pipeline = new Pipeline(device, swapchain, renderpass, vert, frag);
		framebuffer = new Framebuffer(device, swapchain, imageview, renderpass);
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
	
	public void recreateSwapchain() {
		try(MemoryStack stack = MemoryStack.stackPush()) {
			IntBuffer w = stack.mallocInt(1), h = stack.mallocInt(1);
			GLFW.glfwGetFramebufferSize(window.getHandle(), w, h);
			GLFW.glfwWaitEvents();
			vkDeviceWaitIdle(device.device);
			disposeSwapchain();
			createSwapchain(w, h);
			//persp.perspective(70f, ((float)w.get(0))/((float)h.get(0)), 0.001f, 1000f);
		}
	}
	
	long[] imageAvailableSemaphore, renderFinishedSemaphore, inFlightFence;
	private static final int MAX_FRAMES = 1;
	private static int currentFrame = 0;
	private static boolean framebufferResized = false;
	QueueSubmitInfo info = new QueueSubmitInfo();
	Vector3f up = new Vector3f(0,1,0);
	float rot = 0;
	float rotSpeed = 100;
	float x, y;
	Quaternion cameraRot = new Quaternion();
	Quaternion q = new Quaternion();
	@Override
	public void run() {
		InputAPI.pollEvents();
		if(GLFW.glfwGetKey(window.getHandle(), InputAPI.KEY_W) == GLFW.GLFW_PRESS) {
			y += rotSpeed * Time.getDelta();
			if(y > 89f) y = 89f;
		} if(GLFW.glfwGetKey(window.getHandle(), InputAPI.KEY_S) == GLFW.GLFW_PRESS) {
			y -= rotSpeed * Time.getDelta();
			if(y < -89f) y = -89f;
		}
		
		if(GLFW.glfwGetKey(window.getHandle(), InputAPI.KEY_A) == GLFW.GLFW_PRESS) {
			x += rotSpeed * Time.getDelta();
			if(x > 360f) x -= 360f;
		} if(GLFW.glfwGetKey(window.getHandle(), InputAPI.KEY_D) == GLFW.GLFW_PRESS) {
			x -= rotSpeed * Time.getDelta();
			if(x < 0f) x += 360f;
		}
		cameraRot.axisAngle(0, 1, 0, x * Constants.RADIAN).mul(q.axisAngle(1, 0, 0, y * Constants.RADIAN));
		
		try(MemoryStack stack = MemoryStack.stackPush()) {
			IntBuffer ib = stack.mallocInt(1);
			
			long imageAvailableSemaphore = this.imageAvailableSemaphore[currentFrame];
			long renderFinishedSemaphore = this.renderFinishedSemaphore[currentFrame];
			long inFlightFence = this.inFlightFence[currentFrame];
			
			VK10.vkWaitForFences(device.device, inFlightFence, true, Long.MAX_VALUE);
			
			int result = KHRSwapchain.vkAcquireNextImageKHR(device.device, swapchain.swapchain, 0xFFFFFFFFFFFFFFFFL, imageAvailableSemaphore, 0, ib);
			if(result == KHRSwapchain.VK_ERROR_OUT_OF_DATE_KHR) {
				recreateSwapchain();
				return;
			} else if(result != VK10.VK_SUCCESS && result != KHRSwapchain.VK_SUBOPTIMAL_KHR) {
				Debug.critical("API", "Failed to aquire next image!");
			}
			
			int imageIndex = ib.get(0);
			VK10.vkResetFences(device.device, inFlightFence);
//			rot += 90f * Time.getDelta();
//			if(rot >= 360f) rot -= 360f;
//			model.setRotation(up, rot * Constant.RADIAN);
			
			device.waitIdle();
			
			int size = 3 * Matrix4f.SIZEOF;
			//System.out.println(Matrix4f.SIZEOF);
			//System.out.println(Matrix4f.ALIGNOF);
			
			PointerBuffer pb = stack.mallocPointer(1);
			result = vkMapMemory(device.device, cameraUniform.getMemory(), 0, size, 0, pb);
			if(result != VK_SUCCESS)
				Debug.error("API", "Realtime Memory Map failed! Error Code: " + result);
			
			//MemoryUtil.memCopy(camera.address(), pb.get(0), size);
			HotSwap.matrixUpload(pb.get(0), size, cameraRot);
			vkUnmapMemory(device.device, cameraUniform.getMemory());
			size = 4 * 4 * 4;
			result = vkMapMemory(device.device, texturesUniform.getMemory(), 0, size, 0, pb);
			if(result != VK_SUCCESS)
				Debug.error("API", "Realtime Memory Map failed! Error Code: " + result);
			
			FloatBuffer fb = stack.mallocFloat(4 * 4);
			fb.put(0).put(1).put(0).put(1);
			fb.put(0).put(0).put(0).put(0);
			fb.put(0).put(0).put(0).put(0);
			fb.put(0).put(0).put(0).put(0);
			fb.flip();
			MemoryUtil.memCopy(MemoryUtil.memAddress(fb), pb.get(0), size);
			vkUnmapMemory(device.device, texturesUniform.getMemory());
			
			size = 4 * 2 * 4;
			result = vkMapMemory(device.device, lightUniform.getMemory(), 0, size, 0, pb);
			if(result != VK_SUCCESS)
				Debug.error("API", "Realtime Memory Map failed! Error Code: " + result);
			
			fb = stack.mallocFloat(4 * 2);
			fb.put(0).put(0).put(0).put(0);
			fb.put(0).put(0).put(0).put(0);
			fb.flip();
			MemoryUtil.memCopy(MemoryUtil.memAddress(fb), pb.get(0), size);
			vkUnmapMemory(device.device, lightUniform.getMemory());
			
			device.waitIdle();
			
			
			device.waitIdle();
			
			info.pCommandBuffers(stack.pointers(commandBuffers[imageIndex]));
			info.pWaitSemaphores(imageAvailableSemaphore);
			info.pSignalSemaphores(renderFinishedSemaphore);
			graphicsQueue.submit(info, inFlightFence);
			result = graphicsQueue.present(stack, swapchain.swapchain, renderFinishedSemaphore, imageIndex);
			if(result == KHRSwapchain.VK_ERROR_OUT_OF_DATE_KHR || result == KHRSwapchain.VK_SUBOPTIMAL_KHR || framebufferResized) {
				framebufferResized = false;
				recreateSwapchain();
			}
			else if(result != VK10.VK_SUCCESS) Debug.critical("API", "Failed to aquire next image!");
			
			//currentFrame = (currentFrame + 1) % MAX_FRAMES;
		}
	}

	@Override
	public void dispose() {
		device.waitIdle();
		
		device.destroySemaphore(imageAvailableSemaphore);
		device.destroySemaphore(renderFinishedSemaphore);
		device.destroyFence(inFlightFence);
		
		disposeSwapchain();
		
		vertexBuffer.destroy();
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
