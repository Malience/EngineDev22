package engine.rendering;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

import engine.debug.Debug;
import engine.objects.GameObject;
import engine.objects.World;
import math.Transform3D;

public class RenderStructure {
	public static Material[] materials;
	public static Texture[] textures;
	public static Model[] models;
	public static Transform3D[] transforms;
	
	public static int[] modelSegments;
	
	public static int[] materialIndices;
	public static int[] textureIndices;
	
	
	public static void compile() {
		if(World.materials == null) Debug.error("RENDER", "World does not contain materials!");
		if(World.textures == null) Debug.error("RENDER", "World does not contain textures!");
		if(World.models == null) Debug.error("RENDER", "World does not contain models!");
		if(World.objects == null) Debug.error("RENDER", "World does not contain objects!");
		
		
		int numMaterials = World.materials.size();
		int numTextures = World.textures.size();
		int numModels = World.models.size();
		
		
		materials = new Material[numMaterials];
		textures = new Texture[numTextures];
		models = new Model[numModels];
		
		modelSegments = new int[numModels];
		
		HashMap<Material, Integer> matMap = new HashMap<>(numMaterials);
		HashMap<Texture, Integer> texMap = new HashMap<>(numTextures);
		HashMap<Model, Integer> modMap = new HashMap<>(numModels);
		
		for(int i = 0; i < numMaterials; i++) {
			Material m = materials[i] = World.materials.get(i);
			matMap.put(m, i);
		}
		
		for(int i = 0; i < numTextures; i++) {
			Texture t = textures[i] = World.textures.get(i);
			texMap.put(t, i);
		}
		
		for(int i = 0; i < numModels; i++) {
			Model m = models[i] = World.models.get(i);
			modMap.put(m, i);
		}
		
		ArrayList<GameObject> renderableList = new ArrayList<>();
		for(GameObject o : World.objects) {
			if(o.component == null) continue;
			renderableList.add(o);
		}
		
		int numTransforms = renderableList.size();
		
		materialIndices = new int[numTransforms];
		textureIndices = new int[numTransforms];
		transforms = new Transform3D[numTransforms];
		
		//Sorts objects by model index
		Collections.sort(renderableList, new Comparator<GameObject>() {
			@Override
			public int compare(GameObject o1, GameObject o2) {
				return modMap.get(o1.component.model) - modMap.get(o2.component.model);
			}
		});
		
		int modelIndex = 0;
		for(int i = 0; i < numTransforms; i++) {
			GameObject o = renderableList.get(i);
			
			Material mat = o.component.material;
			Texture tex = o.component.texture;
			Model mod = o.component.model;
			Transform3D transform = o.transform;
			
			materialIndices[i] = matMap.get(mat);
			if(tex != null) textureIndices[i] = texMap.get(tex);
			else textureIndices[i] = -1;
			transforms[i] = transform;
			
			if(mod != models[modelIndex]) {
				modelSegments[++modelIndex] = i;
			}
		}
		
	}

}
