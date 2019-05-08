package engine.objects;

import java.util.ArrayList;

import engine.rendering.Renderable;
import math.Transform3D;

public class GameObject {
	public Transform3D transform;
	public GameObject parent;
	public ArrayList<GameObject> children;
	//public arraylist components
	
	public Renderable component;
	
	public GameObject() {
		transform = new Transform3D();
		children = new ArrayList<GameObject>();
	}
	
	public void setParent(GameObject object) {
		removeParent();
		parent = object;
		transform.setParent(object.transform);
		object.children.add(this);
	}
	
	public void removeParent() {
		if(parent == null) return;
		parent.children.remove(this);
		transform.setParent(null);
		parent = null;
	}
	
	public void addComponent(Renderable component) {
		this.component = component;
	}
}
