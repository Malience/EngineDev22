package engine.core;

import engine.messaging.Message;
import engine.multithread.Threads;

public class EngineShutdown extends Message {
	private EngineShutdown() {super(Threads.IMMEDIATE);}
	public static EngineShutdown shutdown() {return new EngineShutdown();}
}
