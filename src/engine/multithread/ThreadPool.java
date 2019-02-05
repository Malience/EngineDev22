package engine.multithread;

import static engine.multithread.ThreadState.*;

import java.util.function.Consumer;
import java.util.function.ObjIntConsumer;

public class ThreadPool {
	private boolean sync;
	private ThreadGroup group;
	
	PoolThread gpuThread;
	GeneralThread[] threads;
	
	private ThreadStructure tasks;
	private ObjIntConsumer<Runnable> add;
	
	ThreadPool(boolean gpu, int threads) {
		tasks = new ThreadStructure();
		add = tasks.register((Runnable r) -> {r.run();});
		
		group = new ThreadGroup("ThreadPool");
		if(threads < 0) threads = Runtime.getRuntime().availableProcessors() - 1;
		if(gpu) {
			threads--;
			//GPUThread init here
		}
		this.threads = new GeneralThread[threads];
		for(int i = 0; i < threads; i++) (this.threads[i] = new GeneralThread(this, group, "GeneralThread - " + i)).start();
	}
	
	void terminate() {
		if(gpuThread != null) gpuThread.dispose();
		for(int i = 0; i < threads.length; i++) threads[i].dispose();
	}
	
	void post(int thread, Runnable task) {add.accept(task, thread);}
	boolean isEmpty(int thread) {return tasks.isEmpty(thread);}
	void run(int thread) {tasks.run(thread);}
	<E> ObjIntConsumer<E> register(Consumer<E> function) {return tasks.register(function);}
	
	public void runMain() {
		tasks.run(Threads.MAIN);
		if(gpuThread == null) tasks.run(Threads.GPU);
		tasks.run(Threads.ANY);
		if(threads.length == 0) tasks.run(Threads.RES);
		sync = true;
		syncLoop: while(sync) {
			if(gpuThread != null && gpuThread.getState() == GENERAL) continue;
			for(int i = 0; i < threads.length; i++) if(threads[i].getState() == GENERAL) continue syncLoop;
			sync = false;
		}
	}
	
	boolean getSync() {return sync;}
}
