package engine;

import engine.messaging.Message;
import engine.multithread.Threads;

public class IOMessage extends Message {
	public final String file;
	public final String action;
	
	private IOMessage(String file, String action) {
		super(Threads.RES);
		this.file = file;
		this.action = action;
	}
	
	//Loads a resource
	public static IOMessage load(String file) {return new IOMessage(file, "LOAD");}
	public static IOMessage unload(String file) {return new IOMessage(file, "UNLOAD");}
	public static IOMessage reload(String file) {return new IOMessage(file, "RELOAD");}
	public static IOMessage save(String file) {return new IOMessage(file, "SAVE");}
}
