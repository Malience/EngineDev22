package engine.messaging;

import java.util.ArrayList;

public class MessageEndpoint {
	public final String name;
	public final int address;
	private MessageBus bus;
	private ArrayList<MessageHandler> handlers;
	//private Queue<MessageContext> messages;
	//TODO: Add in a queue
	
	MessageEndpoint(String name, MessageBus bus, int address) {
		this.name = name;
		this.bus = bus;
		this.address = address;
		handlers = new ArrayList<>();
	}
	
	public void post(Message m) {m.send = address; bus.post(m);}
	public void post(Message m, int... dest) {m.send = address; m.dest = dest; bus.post(m);}
	
	public void recieve(Message m) {
		for(MessageHandler handler : handlers) 
			handler.handle(m);
	}
	
	public void addHandler(MessageHandler h) {handlers.add(h);}
}
