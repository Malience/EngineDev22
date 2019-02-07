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
import java.nio.DoubleBuffer;
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
import api.vulkan.DescriptorLayout;
import api.vulkan.DescriptorPool;
import api.vulkan.DescriptorSet;
import api.vulkan.Device;
import api.vulkan.Framebuffer;
import api.vulkan.HotSwap;
import api.vulkan.Image;
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
import math.Vector4f;

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
	Quaternion rot = new Quaternion();
	float x, y;
	static Matrix4f.Buffer viewProjection;
	static Matrix4f view, projection;
	static {
		viewProjection = Matrix4f.calloc(2);
		view = viewProjection.get(0);
		projection = viewProjection.get(1);
	}
	
	DescriptorLayout desLayout;
	DescriptorPool desPool;
	DescriptorSet[] desSet;
	BufferObject vertexBuffer, indexBuffer, viewProjectionBuffer, lightBuffer;
	BufferObject[] modelBuffer, texturesBuffer;
	
	public void createVertexBuffer() {
		int vdiv = 20; int hdiv = 20;
		vertices = Sphere.generateSphereVertices(0.5f, vdiv, hdiv);
		indices = Sphere.generateSphereIndices(vdiv, hdiv);
		
		vertexBuffer = new BufferObject(physicalDevice, device);
		vertexBuffer.createVertexBuffer(vertices.length * Vertex.SIZEOF);
		vertexBuffer.load(graphicsQueue, commandPool, vertices);
		
		indexBuffer = new BufferObject(physicalDevice, device);
		indexBuffer.createIndexBuffer(indices.limit() * 4);
		indexBuffer.load(graphicsQueue, commandPool, indices);
		
		viewProjectionBuffer = new BufferObject(physicalDevice, device);
		viewProjectionBuffer.createUniformBuffer(16 * 4 * 2);
		
		lightBuffer = new BufferObject(physicalDevice, device);
		lightBuffer.createUniformBuffer(4 * 2 * 4);
		
		float near = 0.1f, far = 40f;
		float aspect = 800f / 600f;
		
		projection.perspective(90f, aspect, near, far);
		view.identity();
		view.translation(0, 0, -12f);
		viewProjectionBuffer.map(viewProjection);
		
		desLayout = new DescriptorLayout(device, 2, 2);
		desPool = new DescriptorPool(device, 96);
		
		desSet = new DescriptorSet[96];
		modelBuffer = new BufferObject[96];
		texturesBuffer = new BufferObject[96];
		
		float cube = 1f;
		float cubeHalf = cube / 2f;
		float face = 2f * cube;
		
		try(MemoryStack stack = MemoryStack.stackPush()) {
			Matrix4f.Buffer model = Matrix4f.mallocStack(96, stack);
			Matrix4f.Buffer texture = Matrix4f.mallocStack(96, stack);
			for(int i = 0; i < 96; i++) model.get(i).identity();
			
			//FRONT FACE
			for(int i = 0; i < 4; i++) {
				for(int j = 0; j < 4; j++) {
					model.get(i * 4 + j).translation((j - 2) * cube + cubeHalf, (i - 2) * cube + cubeHalf, face);
					//texture.get(i * 4 + j).m00(1.0f).m01(1.0f).m02(1.0f).m03(1.0f);
				}
			}
			//RIGHT FACE
			for(int i = 0; i < 4; i++) {
				for(int j = 0; j < 4; j++) {
					model.get(16 + i * 4 + j).translation(face, (i - 2) * cube + cubeHalf, (j - 2) * cube + cubeHalf);
					//texture.get(16 + i * 4 + j).m00(1.0f).m01(1.0f).m02(1.0f).m03(1.0f);
				}
			}
			//BACK FACE
			for(int i = 0; i < 4; i++) {
				for(int j = 0; j < 4; j++) {
					model.get(32 + i * 4 + j).translation((1 - j) * cube + cubeHalf, (i - 2) * cube + cubeHalf, -face);
					//texture.get(32 + i * 4 + j).m00(1.0f).m01(1.0f).m02(1.0f).m03(1.0f);
				}
			}
			//LEFT FACE
			for(int i = 0; i < 4; i++) {
				for(int j = 0; j < 4; j++) {
					model.get(48 + i * 4 + j).translation(-face, (i - 2) * cube + cubeHalf, (1 - j) * cube + cubeHalf);
					//texture.get(48 + i * 4 + j).m00(1.0f).m01(1.0f).m02(1.0f).m03(1.0f);
				}
			}
			//BOTTOM FACE
			for(int i = 0; i < 4; i++) {
				for(int j = 0; j < 4; j++) {
					model.get(64 + i * 4 + j).translation((j - 2) * cube + cubeHalf, -face, (i - 2) * cube + cubeHalf);
					//texture.get(64 + i * 4 + j).m00(1.0f).m01(1.0f).m02(1.0f).m03(1.0f);
				}
			}
			//TOP FACE
			for(int i = 0; i < 4; i++) {
				for(int j = 0; j < 4; j++) {
					model.get(80 + i * 4 + j).translation((j - 2) * cube + cubeHalf, face, (i - 2) * cube + cubeHalf);
					//texture.get(80 + i * 4 + j).m00(1.0f).m01(1.0f).m02(1.0f).m03(1.0f);
				}
			}
			
			
			texture.get(0).m00(0.25f).m03(1.0f); //Red
			texture.get(1).m01(0.25f).m03(1.0f); //Green
			texture.get(2).m01(0.25f).m02(0.25f).m03(1.0f); //Green
			texture.get(3).m02(0.25f).m03(1.0f); //Green
			
			texture.get(4).m00(0.5f).m03(1.0f); //Red
			texture.get(5).m01(0.5f).m03(1.0f); //Green
			texture.get(6).m01(0.5f).m02(0.5f).m03(1.0f); //Green
			texture.get(7).m02(0.5f).m03(1.0f); //Green
			
			texture.get(8).m00(0.75f).m03(1.0f); //Red
			texture.get(9).m01(0.75f).m03(1.0f); //Green
			texture.get(10).m01(0.75f).m02(0.75f).m03(1.0f); //Green
			texture.get(11).m02(0.75f).m03(1.0f); //Green
			
			texture.get(12).m00(1.0f).m03(1.0f); //Red
			texture.get(13).m01(1.0f).m03(1.0f); //Green
			texture.get(14).m01(1.0f).m02(1.0f).m03(1.0f); //Green
			texture.get(15).m02(1.0f).m03(1.0f); //Green
			
			for(int i = 0; i < 96; i++) {
				modelBuffer[i] = new BufferObject(physicalDevice, device);
				modelBuffer[i].createUniformBuffer(16 * 4);
				texturesBuffer[i] = new BufferObject(physicalDevice, device);
				texturesBuffer[i].createUniformBuffer(4 * 4 * 4);
				desSet[i] = new DescriptorSet(device, desPool, desLayout);
				
				desSet[i].bind(viewProjectionBuffer, 0);
				desSet[i].bind(modelBuffer[i], 1);
				desSet[i].bind(texturesBuffer[i], 2);
				desSet[i].bind(lightBuffer, 3);
				
				modelBuffer[i].map(model.get(i));
				texturesBuffer[i].map(texture.get(i));
			}
		
		}
		lightPos.set(0, 0, -12f);
		view.transform(lightPos);
		projection.transform(lightPos);
		v2.get(0).set(lightPos.x(), lightPos.y(), lightPos.z());
		v2.get(1).set(1, 1, 1, 1);
		v2.get(2).set(0, 0, -12, 1);
		lightBuffer.map(v2);
//		int format = Image.findFormat(physicalDevice, 
//				VK10.VK_IMAGE_TILING_OPTIMAL, VK10.VK_FORMAT_FEATURE_DEPTH_STENCIL_ATTACHMENT_BIT, 
//				VK10.VK_FORMAT_D16_UNORM, VK10.VK_FORMAT_D32_SFLOAT, VK10.VK_FORMAT_D32_SFLOAT_S8_UINT, VK10.VK_FORMAT_D24_UNORM_S8_UINT);
//		//int format = VK10.VK_FORMAT_D32_SFLOAT;
//		System.out.println(format);
//		image = Image.createImage(physicalDevice, device, 800, 600, format);
//		//Image.transition(commandPool.createBuffer(), graphicsQueue, image, format, VK10.VK_IMAGE_LAYOUT_UNDEFINED, VK10.VK_IMAGE_LAYOUT_DEPTH_STENCIL_ATTACHMENT_OPTIMAL);
//		imageMemory = Image.allocateImage(physicalDevice, device, image, VK10.VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT);
//		imageView = Image.createImageView(device, image, format);
		
	}
	Vector4f.Buffer v2 = Vector4f.calloc(3);
	Vector3f lightPos = new Vector3f();
	Matrix4f lightMatrix = new Matrix4f();
	long image, imageMemory, imageView;
	
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
			for(int j = 0; j < 96; j++) {
				if(desSet[j] == null) break;
				cb.bindUniforms(pipeline.layout, stack.longs(desSet[j].set));
				cb.draw(indices.limit());
			}
			cb.endRenderPass();
			cb.end();
		}
		}
	}
	
	public void createSwapchain() {
		swapchain = new Swapchain(device, surface);
		imageview = new Imageview(device, swapchain);
		renderpass = new Renderpass(device, swapchain);
		pipeline = new Pipeline(device, swapchain, renderpass, desLayout, vert, frag);
		framebuffer = new Framebuffer(device, swapchain, imageview, renderpass, imageView);
		recordCommandBuffer();
	}
	
	public void createSwapchain(IntBuffer width, IntBuffer height) {
		swapchain = new Swapchain(device, surface, width, height);
		imageview = new Imageview(device, swapchain);
		renderpass = new Renderpass(device, swapchain);
		pipeline = new Pipeline(device, swapchain, renderpass, desLayout, vert, frag);
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
	boolean held = false;
	float origx, origy;
	float rotx, roty;
	float lightrotx, lightroty;
	@Override
	public void run() {
		try(MemoryStack stack = MemoryStack.stackPush()) {
			InputAPI.pollEvents();
			if(GLFW.glfwGetMouseButton(window.getHandle(), GLFW.GLFW_MOUSE_BUTTON_1) == GLFW.GLFW_PRESS) {
				DoubleBuffer x = stack.mallocDouble(1), y = stack.mallocDouble(1);
				GLFW.glfwGetCursorPos(window.getHandle(), x, y);
				GLFW.glfwSetCursorPos(window.getHandle(), 0, 0);
				if(!held) {
					GLFW.glfwSetInputMode(window.getHandle(), GLFW.GLFW_CURSOR, GLFW.GLFW_CURSOR_HIDDEN);
					origx = (float)x.get(0); origy = (float)y.get(0);
					held = true;
				} else {
					Quaternion q = new Quaternion();
					float rotSpeed = 5;
					rotx += ((float) x.get(0)) * rotSpeed * Time.getDelta();
					roty += ((float) y.get(0)) * rotSpeed * Time.getDelta();
					if(roty > 89) roty = 89;
					if(roty < -89) roty = -89;
					rot.axisAngle(0, 1, 0, rotx * Constants.RADIAN).mul(q.axisAngle(0, 0, 1, roty * Constants.RADIAN), rot);
					view.setRotation(rot);
					view.translation(0, 0, -12f);
					viewProjectionBuffer.map(viewProjection);
					Vector3f v = new Vector3f();
					view.transform(v);
					projection.transform(v);
					v2.get(2).x(v.x()).y(v.y()).z(v.z());
					lightBuffer.map(v2);
				}
			}
			if(GLFW.glfwGetMouseButton(window.getHandle(), GLFW.GLFW_MOUSE_BUTTON_1) == GLFW.GLFW_RELEASE && held) {
				GLFW.glfwSetInputMode(window.getHandle(), GLFW.GLFW_CURSOR, GLFW.GLFW_CURSOR_NORMAL);
				GLFW.glfwSetCursorPos(window.getHandle(), origx, origy);
				held = false;
			}
			float rotSpeed = 100;
			boolean lightMoved = false;
			if(GLFW.glfwGetKey(window.getHandle(), GLFW.GLFW_KEY_J) == GLFW.GLFW_PRESS) {
				lightMoved = true;
				lightrotx += rotSpeed * Time.getDelta();
			}
			if(GLFW.glfwGetKey(window.getHandle(), GLFW.GLFW_KEY_L) == GLFW.GLFW_PRESS) {
				lightMoved = true;
				lightrotx -= rotSpeed * Time.getDelta();
			}
			if(GLFW.glfwGetKey(window.getHandle(), GLFW.GLFW_KEY_I) == GLFW.GLFW_PRESS) {
				lightMoved = true;
				lightroty += rotSpeed * Time.getDelta();
			}
			if(GLFW.glfwGetKey(window.getHandle(), GLFW.GLFW_KEY_K) == GLFW.GLFW_PRESS) {
				lightMoved = true;
				lightroty -= rotSpeed * Time.getDelta();
			}
			if(lightMoved) {
				lightPos.set(0, 0, -12f);
				Quaternion q = new Quaternion();
				rot.axisAngle(0, 1, 0, lightrotx * Constants.RADIAN).mul(q.axisAngle(0, 0, 1, lightroty * Constants.RADIAN), rot);
				rot.transform(lightPos);
				view.transform(lightPos);
				projection.transform(lightPos);
				v2.get(0).set(lightPos.x(), lightPos.y(), lightPos.z());
				lightBuffer.map(v2);
			}
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
