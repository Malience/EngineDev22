package engine.core;

import engine.Engine;
import engine.debug.Debug;
import engine.messaging.MessageSystem;
import engine.multithread.ThreadSystem;
import engine.messaging.Message;

public class CoreEngine extends Engine {
	private boolean running = false;
	private int framesPerSecond = 0;
	private Engine engines[];
	
	public CoreEngine(Engine... engines) {super("Core Engine"); this.engines = engines;}
	
	public void start() {
		//Time.setFPS(framesPerSecond);
		for(int i = 0; i < engines.length; i++) engines[i].start();
		MessageSystem.createEndpoint("Core Engine", (Message m) -> {
			if(m instanceof EngineShutdown) this.shutdown();
		});
		super.start();
	}
	
	public void run() {
		super.run();
		running = true;
		//while(running) 
		try { //Prevent Crashes
			while(running) {
				if(Time.processFrame()) {
					for(int i = 0; i < engines.length; i++) {
						engines[i].run();
						if(ThreadSystem.multithread()) ThreadSystem.run();
					}
				}
			}
		} catch(Exception e) {Debug.critical(e);}
		this.dispose();
	}
	
	public void shutdown() {running = false;}
	
	public void dispose() {
		for(int i = 0; i < engines.length; i++) engines[i].dispose();
		super.dispose();
	}
}
