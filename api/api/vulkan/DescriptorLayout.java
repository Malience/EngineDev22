package api.vulkan;

import static org.lwjgl.vulkan.VK10.*;

import java.nio.LongBuffer;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkDescriptorSetLayoutBinding;
import org.lwjgl.vulkan.VkDescriptorSetLayoutCreateInfo;
import org.lwjgl.vulkan.VkDevice;

import engine.debug.Debug;
//TODO: This is going to need a builder class to do more complex things
public class DescriptorLayout {
	private final VkDevice device;
	public final long layout;
	
	public DescriptorLayout(Device device, int vertexDescriptors) {
		this.device = device.device;
		try(MemoryStack stack = MemoryStack.stackPush()) {
			VkDescriptorSetLayoutBinding base = VkDescriptorSetLayoutBinding.calloc()
			.descriptorType(VK10.VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER)
			.descriptorCount(1)
			.stageFlags(VK10.VK_SHADER_STAGE_ALL_GRAPHICS)//TODO: Fix this whole mess
			.pImmutableSamplers(null);	
			VkDescriptorSetLayoutBinding.Buffer desLayout = VkDescriptorSetLayoutBinding.callocStack(vertexDescriptors, stack);
			for(int i = 0; i < vertexDescriptors; i++) desLayout.get(i).set(base).binding(i);
			
			VkDescriptorSetLayoutCreateInfo layoutinfo = VkDescriptorSetLayoutCreateInfo.callocStack(stack)
			.sType(VK_STRUCTURE_TYPE_DESCRIPTOR_SET_LAYOUT_CREATE_INFO)
			.pBindings(desLayout);
			LongBuffer lb = stack.mallocLong(1);
			if(vkCreateDescriptorSetLayout(this.device, layoutinfo, null, lb) != VK_SUCCESS)
				Debug.error("API", "Failed to create descriptor set layout!");
			layout = lb.get(0);
		}
	}
	
	public DescriptorLayout(Device device, int vertexDescriptors, int fragmentDescriptors) {
		this.device = device.device;
		try(MemoryStack stack = MemoryStack.stackPush()) {
			VkDescriptorSetLayoutBinding base = VkDescriptorSetLayoutBinding.calloc()
			.descriptorType(VK10.VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER)
			.descriptorCount(1)
			.stageFlags(VK10.VK_SHADER_STAGE_ALL_GRAPHICS)//TODO: Fix this whole mess
			.pImmutableSamplers(null);	
			VkDescriptorSetLayoutBinding.Buffer desLayout = VkDescriptorSetLayoutBinding.callocStack(vertexDescriptors + fragmentDescriptors, stack);
			for(int i = 0; i < vertexDescriptors; i++) desLayout.get(i).set(base).binding(i);
			base.stageFlags(VK10.VK_SHADER_STAGE_FRAGMENT_BIT);
			for(int i = vertexDescriptors; i < vertexDescriptors + fragmentDescriptors; i++) 
				desLayout.get(i).set(base).binding(i);
			
			VkDescriptorSetLayoutCreateInfo layoutinfo = VkDescriptorSetLayoutCreateInfo.callocStack(stack)
			.sType(VK_STRUCTURE_TYPE_DESCRIPTOR_SET_LAYOUT_CREATE_INFO)
			.pBindings(desLayout);
			LongBuffer lb = stack.mallocLong(1);
			if(vkCreateDescriptorSetLayout(this.device, layoutinfo, null, lb) != VK_SUCCESS)
				Debug.error("API", "Failed to create descriptor set layout!");
			layout = lb.get(0);
		}
	}
	
	public DescriptorLayout(Device device, int vertexDescriptors, int fragmentDescriptors, int samplers) {
		this.device = device.device;
		try(MemoryStack stack = MemoryStack.stackPush()) {
			VkDescriptorSetLayoutBinding base = VkDescriptorSetLayoutBinding.calloc()
			.descriptorType(VK10.VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER)
			.descriptorCount(1)
			.stageFlags(VK10.VK_SHADER_STAGE_ALL_GRAPHICS)//TODO: Fix this whole mess
			.pImmutableSamplers(null);	
			VkDescriptorSetLayoutBinding.Buffer desLayout = VkDescriptorSetLayoutBinding.callocStack(vertexDescriptors + fragmentDescriptors + samplers, stack);
			for(int i = 0; i < vertexDescriptors; i++) desLayout.get(i).set(base).binding(i);
			
			base.stageFlags(VK10.VK_SHADER_STAGE_FRAGMENT_BIT);
			for(int i = vertexDescriptors; i < vertexDescriptors + fragmentDescriptors; i++) 
				desLayout.get(i).set(base).binding(i);
			
			base.descriptorType(VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER);
			for(int i = vertexDescriptors + fragmentDescriptors; i < vertexDescriptors + fragmentDescriptors + samplers; i++)
				desLayout.get(i).set(base).binding(i);
				
			
			VkDescriptorSetLayoutCreateInfo layoutinfo = VkDescriptorSetLayoutCreateInfo.callocStack(stack)
			.sType(VK_STRUCTURE_TYPE_DESCRIPTOR_SET_LAYOUT_CREATE_INFO)
			.pBindings(desLayout);
			LongBuffer lb = stack.mallocLong(1);
			if(vkCreateDescriptorSetLayout(this.device, layoutinfo, null, lb) != VK_SUCCESS)
				Debug.error("API", "Failed to create descriptor set layout!");
			layout = lb.get(0);
		}
	}
	
	public DescriptorLayout(Device device, int vertexDescriptors, int fragmentDescriptors, boolean special) {
		this.device = device.device;
		try(MemoryStack stack = MemoryStack.stackPush()) {
			VkDescriptorSetLayoutBinding base = VkDescriptorSetLayoutBinding.calloc()
			.descriptorType(VK10.VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER)
			.descriptorCount(1)
			.stageFlags(VK10.VK_SHADER_STAGE_ALL_GRAPHICS)//TODO: Fix this whole mess
			.pImmutableSamplers(null);	
			VkDescriptorSetLayoutBinding.Buffer desLayout = VkDescriptorSetLayoutBinding.callocStack(vertexDescriptors + fragmentDescriptors + 2, stack);
			for(int i = 0; i < vertexDescriptors; i++) desLayout.get(i).set(base).binding(i);
			
			base.stageFlags(VK10.VK_SHADER_STAGE_FRAGMENT_BIT);
			for(int i = vertexDescriptors; i < vertexDescriptors + fragmentDescriptors; i++) 
				desLayout.get(i).set(base).binding(i);
			
			int index = vertexDescriptors + fragmentDescriptors;
			base.descriptorType(VK_DESCRIPTOR_TYPE_SAMPLER);
			desLayout.get(index).set(base).binding(index);
			
			index++;
			base.descriptorType(VK_DESCRIPTOR_TYPE_SAMPLED_IMAGE);
			base.descriptorCount(16);//Change to be the number of textures in the array
			desLayout.get(index).set(base).binding(index);
				
			
			VkDescriptorSetLayoutCreateInfo layoutinfo = VkDescriptorSetLayoutCreateInfo.callocStack(stack)
			.sType(VK_STRUCTURE_TYPE_DESCRIPTOR_SET_LAYOUT_CREATE_INFO)
			.pBindings(desLayout);
			LongBuffer lb = stack.mallocLong(1);
			if(vkCreateDescriptorSetLayout(this.device, layoutinfo, null, lb) != VK_SUCCESS)
				Debug.error("API", "Failed to create descriptor set layout!");
			layout = lb.get(0);
		}
	}
	
	public void dispose() {VK10.vkDestroyDescriptorSetLayout(device, layout, null);}
	
}
