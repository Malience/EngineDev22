package engine.messaging;

import java.util.HashSet;
import java.util.Set;
import java.util.function.ObjIntConsumer;

import engine.SubSystem;
import engine.debug.Debug;
import engine.multithread.ThreadSystem;
import engine.multithread.Threads;

public final class MessageSystem extends SubSystem implements MessageBus {
	private static MessageSystem system;
	
	private Set<Message> debug;
	
	private MessageEndpoint[] endpoints;
	private int next;
	private static ObjIntConsumer<Message> add;
	
	public MessageSystem() {
		super("Messaging");
		MessageSystem.system = this;
		debug = new HashSet<>();
	}
	
	@Override
	public void init() {
		endpoints = new MessageEndpoint[20];
	}
	
	public static void register() {
		if(add != null) return;
		add = ThreadSystem.register((Message m) -> {
			system.send(m);
		});
	}

	@Override
	public void terminate() {
		if(system != null) {
			system = null;
			Debug.log("SYSTEM", "Messaging terminated");
		}
	}
	
	@Override
	public void post(Message m) {
		if(m.thread != Threads.IMMEDIATE && add != null && ThreadSystem.multithread()) {
			add.accept(m, m.thread);
		} else send(m);
	}
	
	private void send(Message m) {
		if(m.dest == null) for(int i = 0; i < next; i++) endpoints[i].recieve(m);
		else for(int i = 0; i < m.dest.length; i++) {
			if(m.dest[i] < 0) {debug.add(m); continue;}
			endpoints[m.dest[i]].recieve(m);
		}
	}
	
	static void postMessage(Message m) {
		m.send = -1;
		system.post(m);
	}
	
	public static MessageEndpoint createEndpoint(String name) {return system.registerEndpoint(name);}
	public static MessageEndpoint createEndpoint(String name, MessageHandler handler) {
		MessageEndpoint endpoint = system.registerEndpoint(name);
		endpoint.addHandler(handler);
		return endpoint;
	}
	
	private MessageEndpoint registerEndpoint(String name) {return (endpoints[next] = new MessageEndpoint(name, this, next++));}
	
	public static int getEndpointID(String name) {return system.getID(name);}
	public int getID(String name) {
		for(int i = 0; i < next; i++) if(endpoints[i].name.equals(name)) return i;
		return -1;
	}
	
	static int[] getID(String... name) {
		int[] out = new int[name.length];
		loop: for(int i = 0; i < name.length; i++) {
			for(int j = 0; j < system.next; j++) {
				if(system.endpoints[j].name.equals(name[i])) {
					out[i] = j;
					continue loop;
				}
			}
			out[i] = -1;
			Debug.error("MESSAGING", "No endpoint with name: " + name[i]);
		}
		return out;
	}
}
