package engine.rendering;

import static org.lwjgl.vulkan.VK10.vkDeviceWaitIdle;

import java.nio.DoubleBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.KHRSwapchain;
import org.lwjgl.vulkan.VK10;

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

public class VulkanRenderingEngine extends RenderingEngine {
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
	
	DescriptorLayout desLayout;
	DescriptorPool desPool;
	DescriptorSet desSet;
	BufferObject viewProjectionBuffer, lightBuffer;
	BufferObject modelBuffer, texturesBuffer;
	Matrix4f.Buffer texture;
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
		
		lightBuffer = new BufferObject(physicalDevice, device);
		lightBuffer.createUniformBuffer(4 * 3 * 4);
		
		mainCamera = new Camera();
		
		mainCamera.setPerspective(90f, 800f, 600f, 0.1f, 40f);
		mainCamera.view.translation(0, 0, distance);
		viewProjectionBuffer.map(mainCamera.viewProjection);
		
		desLayout = new DescriptorLayout(device, 2, 2);
		desPool = new DescriptorPool(device, 96);
		
		desSet = new DescriptorSet(device, desPool, desLayout);
		modelBuffer = new BufferObject(physicalDevice, device);
		texturesBuffer = new BufferObject(physicalDevice, device);
		
		float cube = 3f;
		float cubeHalf = cube / 2f;
		float face = 2f * cube;
		
		try(MemoryStack stack = MemoryStack.stackPush()) {
			Matrix4f.Buffer model = Matrix4f.callocStack(96, stack);
			texture = Matrix4f.calloc(96);
			for(int i = 0; i < 96; i++) model.get(i).identity();
			
			//FRONT FACE
			for(int i = 0; i < 4; i++) {
				for(int j = 0; j < 4; j++) {
					int index = i * 4 + j;
					model.get(index).translation((j - 2) * cube + cubeHalf, (i - 2) * cube + cubeHalf, face);
					float intensity = (j + 1) * 0.25f;
					if(i == 0) texture.get(index).m00(intensity).m10(0.0f).m20(0.0f).m30(1.0f);
					if(i == 1) texture.get(index).m00(0.0f).m10(intensity).m20(0.0f).m30(1.0f);
					if(i == 2) texture.get(index).m00(0.0f).m10(0.0f).m20(intensity).m30(1.0f);
					if(i == 3) texture.get(index).m00(intensity).m10(intensity).m20(intensity).m30(1.0f);
				}
			}
			//RIGHT FACE
			for(int i = 0; i < 4; i++) {
				for(int j = 0; j < 4; j++) {
					int index = 16 + i * 4 + j;
					model.get(index).translation(face, (i - 2) * cube + cubeHalf, (1 - j) * cube + cubeHalf);
					float intensity = (j + 1) * 0.25f;
					if(i == 0) texture.get(index).m01(intensity).m11(0.0f).m21(0.0f).m31(1.0f);
					if(i == 1) texture.get(index).m01(0.0f).m11(intensity).m21(0.0f).m31(1.0f);
					if(i == 2) texture.get(index).m01(0.0f).m11(0.0f).m21(intensity).m31(1.0f);
					if(i == 3) texture.get(index).m01(intensity).m11(intensity).m21(intensity).m31(1.0f);
				}
			}
			//BACK FACE
			for(int i = 0; i < 4; i++) {
				for(int j = 0; j < 4; j++) {
					int index = 32 + i * 4 + j;
					model.get(index).translation((1 - j) * cube + cubeHalf, (i - 2) * cube + cubeHalf, -face);
					texture.get(index).m00((i + 1) * 0.25f).m10(0.0f).m20(0.0f).m30(1.0f);
					texture.get(index).m01((j + 1) * 0.25f).m11(0.0f).m21(0.0f).m31(1.0f);
				}
			}
			//LEFT FACE
			for(int i = 0; i < 4; i++) {
				for(int j = 0; j < 4; j++) {
					int index = 48 + i * 4 + j;
					model.get(index).translation(-face, (i - 2) * cube + cubeHalf, (1 - j) * cube + cubeHalf);
					texture.get(index).m00(0.2f).m10(0.0f).m20(0.0f).m30(1.0f);//Ambient
					texture.get(index).m01(0.5f).m11(0.0f).m21(0.0f).m31(1.0f);//Diffuse
					texture.get(index).m03(0.0f).m13((i + 1) * 0.25f).m23((j + 1) * 0.25f).m33(1.0f);//Emissive
				}
			}
			emissiveAlt.get(0, texture.get(48));
			emissiveAlt.get(1, texture.get(49));
			emissiveAlt.get(2, texture.get(50));
			emissiveAlt.get(3, texture.get(51));
			
			//BOTTOM FACE
			for(int i = 0; i < 4; i++) {
				for(int j = 0; j < 4; j++) {
					int index = 64 + i * 4 + j;
					model.get(index).translation((j - 2) * cube + cubeHalf, -face, (i - 2) * cube + cubeHalf);
					texture.get(index).m00(0.2f).m10(0.0f).m20(0.0f).m30(1.0f);//Ambient
					texture.get(index).m01(0.5f).m11(0.0f).m21(0.0f).m31(1.0f);//Diffuse
					texture.get(index).m02(0.0f).m12((i + 1) * 0.25f).m22(0.0f).m32((float)Math.pow(2, j));//Specular
				}
			}
			//TOP FACE
			for(int i = 0; i < 4; i++) {
				for(int j = 0; j < 4; j++) {
					int index = 80 + i * 4 + j;
					model.get(index).translation((j - 2) * cube + cubeHalf, face, (i - 2) * cube + cubeHalf);
					texture.get(index).m00(0.0f).m10((j + 1) * 0.25f).m20((i + 1) * 0.25f).m30(1.0f);//Ambient
					texture.get(index).m01((i - j + 3) * 0.25f).m11(0.0f).m21(0.0f).m31(1.0f);//Diffuse
					texture.get(index).m03((i + 1) * 0.25f).m13(0.5f).m23((j + 1) * 0.25f).m33(1.0f);//Emissive
				}
			}
			
			//for(int i = 0; i < 96; i++) {
				modelBuffer = new BufferObject(physicalDevice, device);
				modelBuffer.createUniformBuffer(96 * 16 * 4);
				texturesBuffer = new BufferObject(physicalDevice, device);
				texturesBuffer.createUniformBuffer(96 * 4 * 4 * 4);
				desSet = new DescriptorSet(device, desPool, desLayout);
				
				desSet.bindBuffer(viewProjectionBuffer, 0);
				desSet.bindBuffer(modelBuffer, 1);
				desSet.bindBuffer(texturesBuffer, 2);
				desSet.bindBuffer(lightBuffer, 3);
				
				modelBuffer.map(model);
				texturesBuffer.map(texture);
			//}
			
		}
		lightPos.set(0, 0, 20f);
		mainCamera.view.transform(lightPos);
		//projection.transform(lightPos);
		v2.get(0).set(lightPos.x(), lightPos.y(), lightPos.z());
		v2.get(1).set(1, 1, 1, 1);
		v2.get(2).set(0, 0, -12, 1);
		lightBuffer.map(v2);
//		int format = Image.findFormat(physicalDevice, 
//				VK10.VK_IMAGE_TILING_OPTIMAL, VK10.VK_FORMAT_FEATURE_DEPTH_STENCIL_ATTACHMENT_BIT, 
//				VK10.VK_FORMAT_D16_UNORM, VK10.VK_FORMAT_D32_SFLOAT, VK10.VK_FORMAT_D32_SFLOAT_S8_UINT, VK10.VK_FORMAT_D24_UNORM_S8_UINT);
		int format = VK10.VK_FORMAT_D32_SFLOAT_S8_UINT;
//		System.out.println(format);
		image = Image.createImage(physicalDevice, device, 800, 600, format);
		imageMemory = Image.allocateImage(physicalDevice, device, image, VK10.VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT);
		imageView = Image.createImageView(device, image, format);
		//Image.transition(commandPool.createBuffer(), graphicsQueue, image, format, VK10.VK_IMAGE_LAYOUT_UNDEFINED, VK10.VK_IMAGE_LAYOUT_DEPTH_STENCIL_ATTACHMENT_OPTIMAL);
		
		dragon = Model.load(physicalDevice, device, graphicsQueue, commandPool, "Basic Cube 2.obj");
	}
	Model sphere, dragon;
	Matrix4f.Buffer emissiveAlt = Matrix4f.calloc(4);
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
//			cb.bindVertexBuffer(vertexBuffer);
//			cb.bindIndexBuffer(indexBuffer);
			cb.bindVertexBuffer(sphere.vertices);
			cb.bindIndexBuffer(sphere.indices);
			//for(int j = 0; j < 96; j++) {
				//if(desSet[j] == null) break;
				cb.pushConstants(pipeline.layout, VK10.VK_SHADER_STAGE_ALL, 0, stack.ints(0));
				cb.bindUniforms(pipeline.layout, stack.longs(desSet.set));
//				cb.draw(indices.limit(), 96);
				cb.draw(sphere.length, 96);
			//}
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
	
	public void createSwapchain(IntBuffer width, IntBuffer height) {
		swapchain = new Swapchain(device, surface, width, height);
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
	
	public void recreateSwapchain() {
		try(MemoryStack stack = MemoryStack.stackPush()) {
			IntBuffer w = stack.mallocInt(1), h = stack.mallocInt(1);
			GLFW.glfwGetFramebufferSize(window.getHandle(), w, h);
			GLFW.glfwWaitEvents();
			vkDeviceWaitIdle(device.device);
			disposeSwapchain();
			createSwapchain(w, h);
			//projection.perspective(90f, ((float)w.get(0))/((float)h.get(0)), 0.001f, 1000f);
		}
	}
	
	long[] imageAvailableSemaphore, renderFinishedSemaphore, inFlightFence;
	private static final int MAX_FRAMES = 1;
	private static int currentFrame = 0;
	private static boolean framebufferResized = false;
	QueueSubmitInfo info = new QueueSubmitInfo();
	boolean held = false;
	float centerx = 800f/2f, centery = 600f/2f;
	float origx, origy;
	float rotx, roty;
	float lightrotx, lightroty;
	float distance = -12f;
	float eAlt = 0;
	Vector3f cameraPos = new Vector3f();
	Vector3f zero = new Vector3f(0, 0, 0);
	Vector3f up = new Vector3f(0, 1, 0);
	@Override
	public void run() {
		try(MemoryStack stack = MemoryStack.stackPush()) {
			InputAPI.pollEvents();
			//TODO: Move input code out of renderer
			mainCamera.update(window);
			viewProjectionBuffer.map(mainCamera.viewProjection);
			Matrix4f invView = mainCamera.view.invert(new Matrix4f());
			v2.get(2).set(invView.m03(), invView.m13(), invView.m23(), 1);
			lightBuffer.map(v2);
			
			float rotSpeed = 300;
			boolean lightMoved = false;
			if(	GLFW.glfwGetKey(window.getHandle(), GLFW.GLFW_KEY_W) == GLFW.GLFW_PRESS ||
				GLFW.glfwGetKey(window.getHandle(), GLFW.GLFW_KEY_I) == GLFW.GLFW_PRESS) {
				lightMoved = true;
				lightroty += rotSpeed * Time.getDelta();
			}
			if(	GLFW.glfwGetKey(window.getHandle(), GLFW.GLFW_KEY_S) == GLFW.GLFW_PRESS ||
				GLFW.glfwGetKey(window.getHandle(), GLFW.GLFW_KEY_K) == GLFW.GLFW_PRESS) {
				lightMoved = true;
				lightroty -= rotSpeed * Time.getDelta();
			}
			if(	GLFW.glfwGetKey(window.getHandle(), GLFW.GLFW_KEY_A) == GLFW.GLFW_PRESS ||
				GLFW.glfwGetKey(window.getHandle(), GLFW.GLFW_KEY_J) == GLFW.GLFW_PRESS) {
				lightMoved = true;
				lightrotx += rotSpeed * Time.getDelta();
			}
			if(	GLFW.glfwGetKey(window.getHandle(), GLFW.GLFW_KEY_D) == GLFW.GLFW_PRESS ||
				GLFW.glfwGetKey(window.getHandle(), GLFW.GLFW_KEY_L) == GLFW.GLFW_PRESS) {
				lightMoved = true;
				lightrotx -= rotSpeed * Time.getDelta();
			}
			if(lightMoved) {
				lightPos.set(0, 0, -20f);
				Quaternion q = new Quaternion();
				mainCamera.rot.axisAngle(1, 0, 0, lightroty * Constants.RADIAN).mul(q.axisAngle(0, 1, 0, lightrotx * Constants.RADIAN), mainCamera.rot);
				Matrix4f m = new Matrix4f();
				m.setRotation(mainCamera.rot);
				m.transform(lightPos);
				mainCamera.view.transform(lightPos);
				v2.get(0).set(lightPos.x(), lightPos.y(), lightPos.z());
				lightBuffer.map(v2);
				lightMoved = false;
			}
			
			eAlt += 5f * Time.getDelta();
			for(int i = 48; i < 52; i++) {
				texture.get(i).m03(0.0f).m13((float)Math.sin(eAlt)).m23((float)Math.cos(eAlt)).m33(1.0f);//Emissive
				
			}
			texturesBuffer.map(texture);
			//END Input code
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
			//Maybe remove resize code (I don't really need it)
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
