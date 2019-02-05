package engine;

import java.lang.reflect.Constructor;

import engine.messaging.MessageEndpoint;
import engine.messaging.MessageSystem;
import engine.messaging.Message;

public class Systems {
	private MessageEndpoint endpoint;
	private static Systems main;
	private SubSystem[] systems;
	private int next = 0;
	
	@SuppressWarnings("unchecked")
	public static void system_init(int numsystems) {
		if(main != null) return;
		main = new Systems(numsystems);
		main.instantiate(MessageSystem.class);
		main.init("Messaging");
		main.endpoint = MessageSystem.createEndpoint("Systems");
		
		main.endpoint.addHandler((Message m) -> {
			if(m instanceof SystemMessage) {
				SystemMessage s = (SystemMessage) m;
				switch(s.status) {
				case "INIT": main.init(s.name); return;
				case "TERM": main.terminate(s.name); return;
				case "INSTANTIATE": main.instantiate(s.classdef); return;
				}
			}
		});
	}
	
	private Systems(int numsystems) {systems = new SubSystem[numsystems];}
	
	public <E extends SubSystem> void instantiate(Class<E> c) {
		try {
			Constructor<E> cons = c.getDeclaredConstructor();
			SubSystem system = cons.newInstance();
			systems[next++] = system;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void init(String name) {init(getSystem(name));}
	public void init(int id) {init(systems[id]);}
	public <E extends SubSystem> void init(E system) {system.init();}
	
	public void terminate(String name) {terminate(getSystem(name));}
	public void terminate(int id) {terminate(systems[id]);}
	public <E extends SubSystem> void terminate(E system) {system.terminate();}
	
	public int getID(String name) {
		for(int i = 0; i < next; i++) if(systems[i].name.equals(name)) return i;
		return -1;
	}
	
	public SubSystem getSystem(int id) {return systems[id];}
	public SubSystem getSystem(String name) {
		for(int i = 0; i < next; i++) if(systems[i].name.equals(name)) return systems[i];
		return null;
	}
	
	public static void terminate() {for(int i = 0; i < main.next; i++) main.systems[i].terminate();}
}
