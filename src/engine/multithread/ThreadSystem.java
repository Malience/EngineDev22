package engine.multithread;

import java.util.function.Consumer;
import java.util.function.ObjIntConsumer;

import engine.SubSystem;
import engine.debug.Debug;
import engine.messaging.MessageSystem;

public class ThreadSystem extends SubSystem {
	private static ThreadSystem system;
	private ThreadPool threadpool;
	
	public ThreadSystem() {
		super("Thread");
		system = this;
	}
	
	public void init() {
		threadpool = new ThreadPool(false, -1);
		Thread.currentThread().setPriority(10);
		MessageSystem.register();
	}
	public void terminate() {
		if(threadpool != null) {
			threadpool.terminate();
			threadpool = null;
			system = null;
			Debug.log("SYSTEM", "Threading terminated");
		}
	}
	
	public static void run() {system.threadpool.runMain();}
	public static boolean multithread() {return system != null;}
	public static void post(int thread, Runnable task) {system.threadpool.post(thread, task);}
	
	public static <E> ObjIntConsumer<E> register(Consumer<E> function) {return system.threadpool.register(function);}
}
