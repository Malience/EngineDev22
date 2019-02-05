package engine;

import engine.debug.Debug;

public abstract class Engine {
	public final String name;
	protected Engine(String name) {this.name = name;}
	public void start() {Debug.log("ENGINE", name + " online");}
	public void run() {Debug.log("ENGINE", name + " running");}
	public void dispose() {Debug.log("ENGINE", name + " destroyed");}
}
