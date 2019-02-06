package api.vulkan;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkVertexInputAttributeDescription;
import org.lwjgl.vulkan.VkVertexInputBindingDescription;

import math.Vector2f;
import math.Vector3f;

public class Vertex {
	public Vector3f pos;
	public Vector3f normal;
	public Vector2f texCoord;
	
	public Vertex() {}
	public Vertex(float x, float y, float z, float nx, float ny, float nz, float s, float t) {
		pos = new Vector3f(x, y, z);
		normal = new Vector3f(nx, ny, nz);
		texCoord = new Vector2f(s, t);
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
	
	public static VkVertexInputAttributeDescription.Buffer getAttributeDescriptions() {return getAttributeDescriptions(VkVertexInputAttributeDescription.calloc(3));}
	public static VkVertexInputAttributeDescription.Buffer getAttributeDescriptions(MemoryStack stack) {return getAttributeDescriptions(VkVertexInputAttributeDescription.callocStack(3, stack));}
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
		.offset(NORMAL);
		descriptions.get(2)
		.binding(0)
		.location(2)
		.format(VK10.VK_FORMAT_R32G32_SFLOAT)
		.offset(TEXCOORD);
		return descriptions;
	}
	
    public static final int SIZEOF;
    public static final int POS, NORMAL, TEXCOORD;

    static {
        SIZEOF = Vector3f.SIZEOF + Vector3f.SIZEOF + Vector2f.SIZEOF;
        
        POS = 0;
        NORMAL = Vector3f.SIZEOF;
        TEXCOORD = Vector3f.SIZEOF * 2;
    }
    
    public String toString() {
    	return "Pos: " + pos.x() + ", " + pos.y() + ", " + pos.z() + "Normal: " + normal.x() + ", " + normal.y() + ", " + normal.z() + " TexCoord: " + texCoord.x() + ", " + texCoord.y();
    }
}
