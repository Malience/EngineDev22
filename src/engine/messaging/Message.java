package engine.messaging;

import engine.multithread.Threads;

public class Message {
	public final int thread;
	public final String message;
	int send;
	int[] dest;
	
	public Message(int thread) {this(thread, "Message");}
	public Message(int thread, String message) {this.thread = thread; this.message = message;}
	
	public void post() {
		this.dest = null;
		MessageSystem.postMessage(this);
	}
	public void post(int... dest) {
		this.dest = dest;
		MessageSystem.postMessage(this);
	}
	
	public int getSender() {return send;}
	public void returnTo(Message m) {dest = new int[]{m.send};}
	
	public Message setDest(int... dest) {this.dest = dest; return this;}
	public Message setDest(String... dest) {this.dest = MessageSystem.getID(dest); return this;}
	
	public static Message main(String message) {return new Message(Threads.MAIN, message);}
	public static Message immediate(String message) {return new Message(Threads.IMMEDIATE, message);}
	public static Message res(String message) {return new Message(Threads.RES, message);}
	public static Message gpu(String message) {return new Message(Threads.GPU, message);}
	public static Message any(String message) {return new Message(Threads.ANY, message);}
	
	@Override
	public String toString() {
		String t = "";
		switch(thread) {
		case Threads.MAIN: t = "MAIN"; break;
		case Threads.IMMEDIATE: t = "IMMEDIATE"; break;
		case Threads.RES: t = "RES"; break;
		case Threads.GPU: t = "GPU"; break;
		case Threads.ANY: t = "ANY"; break;
		}
		return "Thread: " + t + 
				"\nSender: " + send + 
				"\nDestination: " + dest.toString() + 
				"\nMessage: " + message;
	}
}
