package engine.objects;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import org.json.*;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.system.MathUtil;

import api.vulkan.CommandPool;
import api.vulkan.Device;
import api.vulkan.PhysicalDevice;
import api.vulkan.Queue;
import engine.debug.Debug;
import engine.rendering.Material;
import engine.rendering.Model;
import engine.rendering.Renderable;
import engine.rendering.Texture;
import engine.window.Window;
import math.Constants;
import math.Quaternion;
import math.Transform3D;
import math.Vector3f;

public class World {
	private static final String WORLD_LOCATION = "./res/worlds/";
	
	public static ArrayList<GameObject> objects;
	public static ArrayList<Renderable> renderables;
	public static ArrayList<Model> models;
	public static ArrayList<Material> materials;
	public static ArrayList<Texture> textures;
	
	public static GameObject createObject() {
		GameObject object = new GameObject();
		objects.add(object);
		return object;
	}
	
	public static void destroyObject(GameObject object) {
		objects.remove(object);
		for(GameObject o : object.children) o.removeParent();
		object.removeParent();
	}
	
	public static int selection = -1;
	public static boolean translate = false;
	static float tSpeed = 0.1f, rSpeed = 0.1f;
	
	public static boolean updateSelection(Window window) {
		if(objects == null) return false;
		
		boolean changed = false;
		long handle = window.getHandle();
		if(GLFW.glfwGetKey(handle, GLFW.GLFW_KEY_KP_MULTIPLY) == GLFW.GLFW_PRESS) {
			translate = !translate;
		}
		if(GLFW.glfwGetKey(handle, GLFW.GLFW_KEY_KP_ADD) == GLFW.GLFW_PRESS) {
			selection++;
		}
		if(GLFW.glfwGetKey(handle, GLFW.GLFW_KEY_KP_SUBTRACT) == GLFW.GLFW_PRESS) {
			selection--;
		}
		
		if(selection < -1) selection = objects.size() - 1;
		else if(selection == -1) return false;
		else if(selection >= objects.size()) {
			selection = -1;
			return false;
		}
		
		GameObject o = objects.get(selection);
		Transform3D t = o.transform;
		
		float x, y, z;
		x = y = z = 0;
		
		if(GLFW.glfwGetKey(handle, GLFW.GLFW_KEY_KP_8) == GLFW.GLFW_PRESS) {
			x += 1; changed = true;
		}
		if(GLFW.glfwGetKey(handle, GLFW.GLFW_KEY_KP_5) == GLFW.GLFW_PRESS) {
			x -= 1; changed = true;
		}
		if(GLFW.glfwGetKey(handle, GLFW.GLFW_KEY_KP_4) == GLFW.GLFW_PRESS) {
			y += 1; changed = true;
		}
		if(GLFW.glfwGetKey(handle, GLFW.GLFW_KEY_KP_6) == GLFW.GLFW_PRESS) {
			y -= 1; changed = true;
		}
		if(GLFW.glfwGetKey(handle, GLFW.GLFW_KEY_KP_7) == GLFW.GLFW_PRESS) {
			z += 1; changed = true;
		}
		if(GLFW.glfwGetKey(handle, GLFW.GLFW_KEY_KP_9) == GLFW.GLFW_PRESS) {
			z -= 1; changed = true;
		}
		
		if(changed) {
			if(translate) {
				t.translate(x * tSpeed, z * tSpeed, y * tSpeed);
			} else {
				Quaternion q = new Quaternion();
				q.euler(x * rSpeed, y * rSpeed, z * rSpeed);
				t.rotate(q);
			}
		}
		
		return changed;
	}
	
	public static void load(PhysicalDevice physicalDevice, Device device, CommandPool pool, Queue queue, String filename) {
		String s = "";
		try {
		BufferedReader file = new BufferedReader(new FileReader(WORLD_LOCATION + filename));
		String line;
		while((line = file.readLine()) != null) s += line;
		file.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		try {
			JSONObject json = new JSONObject(s);
			
			
			//Load textures
			JSONArray texturesJSON = json.getJSONArray("textures");
			int numTextures = texturesJSON.length();
			Texture[] textureList = new Texture[numTextures];
			if(textures == null) textures = new ArrayList<>(numTextures);
			
			for(int i = 0; i < numTextures; i++) {
				textureList[i] = new Texture(physicalDevice, device, pool, queue, texturesJSON.getString(i));
				textures.add(textureList[i]);
			}
			
			
			//Load models
			JSONArray modelsJSON = json.getJSONArray("models");
			int numModels = modelsJSON.length();
			Model[] modelList = new Model[numModels];
			if(models == null) models = new ArrayList<>(numModels);
			
			for(int i = 0; i < numModels; i++) {
				String modelName = modelsJSON.getString(i);
				if(modelName.contains(".")) modelList[i] = Model.load(physicalDevice, device, queue, pool, modelName);
				else modelList[i] = Model.loadShape(physicalDevice, device, queue, pool, modelName);
				models.add(modelList[i]);
			}
			
			//Load materials
			JSONArray materialsJSON = json.getJSONArray("materials");
			int numMaterials = materialsJSON.length();
			Material[] materialList = new Material[numMaterials];
			if(materials == null) materials = new ArrayList<>(numMaterials);
			
			for(int i = 0; i < numMaterials; i++) {
				JSONObject o = materialsJSON.getJSONObject(i);
				Material m = materialList[i] = new Material();
				
				JSONArray a = o.getJSONArray("ambientColor");
				m.setAmbientColor(a.getFloat(0), a.getFloat(1), a.getFloat(2), a.getFloat(3));
				a = o.getJSONArray("diffuseColor");
				m.setDiffuseColor(a.getFloat(0), a.getFloat(1), a.getFloat(2), a.getFloat(3));
				a = o.getJSONArray("specularColor");
				m.setSpecularColor(a.getFloat(0), a.getFloat(1), a.getFloat(2), a.getFloat(3));
				a = o.getJSONArray("emissiveColor");
				m.setEmissiveColor(a.getFloat(0), a.getFloat(1), a.getFloat(2), a.getFloat(3));
				
				materials.add(m);
			}
			
			//Load Renderables
			JSONArray renderablesJSON = json.getJSONArray("renderables");
			int numRenderables = renderablesJSON.length();
			Renderable[] renderableList = new Renderable[numRenderables];
			if(renderables == null) renderables = new ArrayList<>(numRenderables);
			
			for(int i = 0; i < numRenderables; i++) {
				JSONObject o = renderablesJSON.getJSONObject(i);
				Renderable r = renderableList[i] = new Renderable();
				
				int modelIndex = o.getInt("model");
				int materialIndex = o.getInt("material");
				int textureIndex = o.getInt("texture");

				r.model = modelList[modelIndex];
				r.material = materialList[materialIndex];
				if(textureIndex >=0) r.texture = textureList[textureIndex];
				
				renderables.add(r);
			}
			
			
			//Load objects
			JSONArray objectsJSON = json.getJSONArray("objects");
			int numObjects = objectsJSON.length();
			int[] parentList = new int[numObjects];
			GameObject[] objectList = new GameObject[numObjects];
			if(objects == null) objects = new ArrayList<>(numObjects);
			
			for(int i = 0; i < numObjects; i++) {
				JSONObject o = objectsJSON.getJSONObject(i);
				GameObject g = objectList[i] = new GameObject();
				
				//parent
				parentList[i] = o.getInt("parent");
				
				//transform
				Transform3D transform = g.transform;
				JSONObject t = o.getJSONObject("transform");
				JSONArray a = t.getJSONArray("position");
				transform.translate(a.getFloat(0), a.getFloat(1), a.getFloat(2));
				
				a = t.getJSONArray("rotation");
				transform.rotate(Constants.RADIAN * a.getFloat(0), Constants.RADIAN * a.getFloat(1), Constants.RADIAN * a.getFloat(2));
				
				a = t.getJSONArray("scale");
				transform.scale(a.getFloat(0), a.getFloat(1), a.getFloat(2));
				
				int renderableIndex = o.getInt("renderable");
				if(renderableIndex >= 0) g.component = renderableList[renderableIndex];
				
				objects.add(g);
			}
			
			for(int i = 0; i < numObjects; i++) {
				GameObject g = objectList[i];
				int parent = parentList[i];
				if(parent >= 0) g.setParent(objectList[parent]);
			}
			
			queue.waitIdle();
		} catch(Exception e) {
			Debug.critical("RES", "World file format error!");
		}
	}
}
