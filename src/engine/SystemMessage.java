package engine;

import engine.messaging.Message;
import engine.multithread.Threads;

public class SystemMessage extends Message {
	public final String name;
	@SuppressWarnings("rawtypes")
	public final Class classdef;
	public final String status;
	
	private <E extends SubSystem> SystemMessage(String name, Class<E> classdef, String status) {
		super(Threads.IMMEDIATE);
		this.name = name;
		this.classdef = classdef;
		this.status = status;
	}
	
	public static SystemMessage init(String name) {return new SystemMessage(name, null, "INIT");}
	public static SystemMessage term(String name) {return new SystemMessage(name, null, "TERM");}
	public static <E extends SubSystem> SystemMessage instantiate(Class<E> classdef) {return new SystemMessage(null, classdef, "INSTANTIATE");}
}
