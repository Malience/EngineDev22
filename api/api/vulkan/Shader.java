package api.vulkan;

import java.nio.ByteBuffer;
import java.nio.LongBuffer;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkShaderModuleCreateInfo;

import engine.debug.Debug;
import engine.io.IO;
import util.SPIRV;

public class Shader {
	public static final int VERTEX_BIT = VK10.VK_SHADER_STAGE_VERTEX_BIT;
	public static final int FRAGMENT_BIT = VK10.VK_SHADER_STAGE_FRAGMENT_BIT;
	public static final int GEOMETRY_BIT = VK10.VK_SHADER_STAGE_GEOMETRY_BIT;
	public static final int TESSELATION_CONTROL_BIT = VK10.VK_SHADER_STAGE_TESSELLATION_CONTROL_BIT;
	public static final int TESSELATION_EVALUATION_BIT = VK10.VK_PIPELINE_STAGE_TESSELLATION_EVALUATION_SHADER_BIT;
	public static final int COMPUTE_BIT = VK10.VK_SHADER_STAGE_COMPUTE_BIT;
	
	private final VkDevice device;
	
	public final String name;
	public final long module;
	public final int stage;
	
	public Shader(Device device, String filename, int stage) {
		this.device = device.device;
		
		name = filename;
		ByteBuffer code = SPIRV.compileFile(filename);//IO.loadShader("./res/shaders/" + filename + ".spv");
		try(MemoryStack stack = MemoryStack.stackPush()) {
			VkShaderModuleCreateInfo shaderinfo = VkShaderModuleCreateInfo.callocStack(stack)
			.sType(VK10.VK_STRUCTURE_TYPE_SHADER_MODULE_CREATE_INFO)
			.pCode(code);
			
			LongBuffer lb = stack.mallocLong(1);
			if(VK10.vkCreateShaderModule(this.device, shaderinfo, null, lb) != VK10.VK_SUCCESS) {
				System.err.println("Vertex Shader failed to compile!");
			}
			module = lb.get(0);
		}
		
		this.stage = stage;
		
		Debug.info("API", "Shader: " + name + " has been instantiated");
	}
	
//	public static long createShader(ByteBuffer shader) {return createShader(stack, shader);}}
//	public static long createShader(MemoryStack stack, ByteBuffer shader) {
//		
//		return lb.get(0);
//	}
//	
//	public static void destroyShader(long module) {}
	
	public void dispose() {
		VK10.vkDestroyShaderModule(this.device, module, null);
		Debug.info("API", "Shader: " + name + " has been disposed");
	}
}
