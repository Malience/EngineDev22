package engine.window;

import api.InputAPI;
import engine.SubSystem;
import engine.core.EngineShutdown;
import engine.debug.Debug;
import engine.messaging.LongMessage;
import engine.messaging.Message;
import engine.messaging.MessageEndpoint;
import engine.messaging.MessageSystem;
import engine.window.Window;

public class WindowSystem extends SubSystem {
	private Window main;
	private MessageEndpoint endpoint;
	
	public WindowSystem() {
		super("Window");
	}
	
	@Override
	public void init() {
		InputAPI.init();
		main = new Window("test", 800, 600);
		
		InputAPI.setCloseCallback(main.getHandle(), (long window) -> {
			EngineShutdown.shutdown().post();
		});
		
		endpoint = MessageSystem.createEndpoint("Window");
		endpoint.addHandler((Message m)->{
			switch(m.message) {
			case "Swap": main.swapBuffers(); return;
			case "Show": main.show(); return;
			case "GetMainWindow":
				endpoint.post(LongMessage.create(m.thread, "Window ID", main.getHandle()), m.getSender());
				return;
			}
		});
	}

	@Override
	public void terminate() {
		main.dispose();
		InputAPI.dispose();
		Debug.log("SYSTEM", "Windows terminated");
	}
}
