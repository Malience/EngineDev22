package engine;

import engine.debug.Debug;

public abstract class SubSystem {
	final String name;
	public SubSystem(String name) {this.name = name; Debug.log("SYSTEM", name + " created");}
	public abstract void init();
	public abstract void terminate();
	public String getName() {return name;}
}
 