package engine.input;

import api.InputAPI;
import api.InputAPI.KeyCallback;
import engine.Engine;
import engine.messaging.LongMessage;
import engine.messaging.Message;
import engine.messaging.MessageEndpoint;
import engine.messaging.MessageSystem;
import engine.multithread.Threads;

public class InputEngine extends Engine {
	private MessageEndpoint endpoint;
	private KeyCallback keycallback;
	
	public InputEngine() {super("Input Engine"); endpoint = MessageSystem.createEndpoint("Input");}

	@Override
	public void start() {
		InputAPI.setDefaultErrorCallback();
		Input.init();
		
		keycallback = (int button, int action, int mods) -> {
			Input.key(button, action);
		};
		
		endpoint.addHandler((Message m) -> {
			if(m.thread != Threads.MAIN) return;
			if(m instanceof LongMessage) {
				LongMessage l = (LongMessage) m;
				switch(l.message) {
				case "Window ID":
					InputAPI.setKeyCallback(l.value, keycallback);
					break;
				default:
				}
			}
		});
		
		endpoint.post(Message.main("GetMainWindow"));
	}

	@Override
	public void run() {
		Input.reset();
		InputAPI.pollEvents();
	}

	@Override
	public void dispose() {
		Input.dispose();
	}

}
