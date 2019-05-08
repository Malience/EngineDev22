package engine.rendering;

import java.nio.IntBuffer;

import org.lwjgl.PointerBuffer;
import org.lwjgl.assimp.AIFace;
import org.lwjgl.assimp.AIMesh;
import org.lwjgl.assimp.AIScene;
import org.lwjgl.assimp.AIVector2D;
import org.lwjgl.assimp.AIVector3D;
import org.lwjgl.assimp.Assimp;
import org.lwjgl.system.MemoryStack;

import api.vulkan.BufferObject;
import api.vulkan.CommandPool;
import api.vulkan.Device;
import api.vulkan.PhysicalDevice;
import api.vulkan.Queue;
import api.vulkan.Vertex;
import engine.debug.Debug;

public class Model {
	private static final String MODEL_PATH = "./res/meshes/";
	public final String name;
	
	BufferObject vertices;
	BufferObject indices;
	int length;
	
	public void dispose() {
		vertices.destroy();
		indices.destroy();
	}
	
	public Model(String name) {
		this.name = name;
	}
	
	public static Model loadShape(PhysicalDevice physicalDevice, Device device, Queue queue, CommandPool pool, String filename) {
		Vertex[] vertices = null;
		IntBuffer indices = null;
		switch(filename.toUpperCase()) {
		case "SPHERE":
			vertices = Shape.generateSphereVertices(1f, 8, 8);
			indices = Shape.generateSphereIndices(8, 8);
		}
		if(vertices == null || indices == null) {
			Debug.error("RES", "Shape " + filename + " not found!");
			return null;
		}
		
		Model model = new Model(filename);
		
		model.vertices = new BufferObject(physicalDevice, device);
		model.vertices.createVertexBuffer(vertices.length * Vertex.SIZEOF);
		model.vertices.load(queue, pool, vertices);
		
		model.indices = new BufferObject(physicalDevice, device);
		model.indices.createIndexBuffer(indices.limit() * 4);
		model.indices.load(queue, pool, indices);
		
		model.length = indices.remaining();
		
		return model;
	}
	
	public static Model load(PhysicalDevice physicalDevice, Device device, Queue queue, CommandPool pool, String filename) {
		try(MemoryStack stack = MemoryStack.stackPush()) {
			AIScene scene = Assimp.aiImportFile(MODEL_PATH + filename, Assimp.aiProcess_Triangulate | Assimp.aiProcess_FlipUVs); //Just incase someone exported without triangulating
			PointerBuffer pb = scene.mMeshes();
			AIMesh mesh = AIMesh.create(pb.get(0));
			AIVector3D.Buffer vertices = mesh.mVertices();
			AIVector3D.Buffer normals = mesh.mNormals();
			AIVector3D.Buffer texCoords = mesh.mTextureCoords(0);
			AIFace.Buffer indices = mesh.mFaces();
			
			int num = mesh.mNumVertices();
			Vertex[] vArray = new Vertex[num];
			for(int i = 0; i < num; i++) {
				AIVector3D vertex = vertices.get(i);
				AIVector3D normal = normals.get(i);
				AIVector3D texCoord = texCoords.get(i);
				vArray[i] = new Vertex(vertex.x(), vertex.y(), vertex.z(), normal.x(), normal.y(), normal.z(), texCoord.x(), texCoord.y());
			}
			
			int numFaces = mesh.mNumFaces();
			int numIndices = numFaces * 3;
			int[] indexIndices = new int[numIndices];
			IntBuffer ib = stack.mallocInt(numIndices);
			for(int i = 0; i < numFaces; i++) {
				AIFace face = indices.get(i);
				indexIndices[i * 3 + 0] = face.mIndices().get(0);
				indexIndices[i * 3 + 1] = face.mIndices().get(1);
				indexIndices[i * 3 + 2] = face.mIndices().get(2);
				ib.put(face.mIndices());
			}
			ib.flip();
			
			Model model = new Model(filename);
			model.vertices = new BufferObject(physicalDevice, device);
			model.vertices.createVertexBuffer(num * 8 * 4);
			model.vertices.load(queue, pool, vArray);
			
			model.indices = new BufferObject(physicalDevice, device);
			model.indices.createIndexBuffer(numIndices * 4);
			model.indices.load(queue, pool, ib);
			
			model.length = numIndices;
			
			return model;
		}
	}
	
	public String toString() {
		return name;
	}
}
