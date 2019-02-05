package api.vulkan;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkVertexInputAttributeDescription;
import org.lwjgl.vulkan.VkVertexInputBindingDescription;

import math.Vector2f;
import math.Vector3f;

public class Vertex {
	public Vector3f pos;
	public Vector3f color;
	
	public Vertex() {}
	public Vertex(float x, float y, float z, float r, float g, float b) {
		pos = new Vector3f(x, y, z);
		color = new Vector3f(r, g, b);
	}
	
	
	public static VkVertexInputBindingDescription.Buffer getBindingDescription() {return getBindingDescription(VkVertexInputBindingDescription.calloc(1));}
	public static VkVertexInputBindingDescription.Buffer getBindingDescription(MemoryStack stack) {return getBindingDescription(VkVertexInputBindingDescription.callocStack(1, stack));}
	private static VkVertexInputBindingDescription.Buffer getBindingDescription(VkVertexInputBindingDescription.Buffer description) {
		description
		.binding(0)
		.stride(SIZEOF)
		.inputRate(VK10.VK_VERTEX_INPUT_RATE_VERTEX);
		return description;
	}
	
	public static VkVertexInputAttributeDescription.Buffer getAttributeDescriptions() {return getAttributeDescriptions(VkVertexInputAttributeDescription.calloc(2));}
	public static VkVertexInputAttributeDescription.Buffer getAttributeDescriptions(MemoryStack stack) {return getAttributeDescriptions(VkVertexInputAttributeDescription.callocStack(2, stack));}
	private static VkVertexInputAttributeDescription.Buffer getAttributeDescriptions(VkVertexInputAttributeDescription.Buffer descriptions) {
		descriptions.get(0)
		.binding(0)
		.location(0)
		.format(VK10.VK_FORMAT_R32G32B32_SFLOAT)
		.offset(POS);
		descriptions.get(1)
		.binding(0)
		.location(1)
		.format(VK10.VK_FORMAT_R32G32B32_SFLOAT)
		.offset(COLOR);
		return descriptions;
	}
	
    public static final int SIZEOF;
    public static final int POS, COLOR;

    static {
        SIZEOF = Vector3f.SIZEOF + Vector3f.SIZEOF;
        
        POS = 0;
        COLOR = Vector3f.SIZEOF;
    }
    
    public String toString() {
    	return "Pos: " + pos.x() + ", " + pos.y() + " Color: " + color.x() + ", " + color.y() + ", " + color.z();
    }
}
