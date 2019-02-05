package engine.multithread;

import java.util.function.Consumer;
import java.util.function.ObjIntConsumer;

import engine.debug.Debug;

public class ThreadStructure {
	@SuppressWarnings("rawtypes")
	private Consumer[] functions;
	private int next;
	private ThreadQueue[] queues;
	
	public ThreadStructure() {
		functions = new Consumer[10];
		queues = new ThreadQueue[Threads.THREAD_TYPES];
		for(int i = 0; i < Threads.THREAD_TYPES; i++) queues[i] = new ThreadQueue(100);
	}
	
	public void run(int thread) {queues[thread].run();}
	
	private <E> void add(E task, int thread, int function) {queues[thread].add(function, task);}
	
	public boolean isEmpty(int thread) {return queues[thread].isEmpty();}
	
	public <E> ObjIntConsumer<E> register(Consumer<E> function) {
		if(next >= functions.length) {
			Debug.error("THREAD", "ThreadStructure function registry overflow!");
			return null;
		}
		int i = next++;
		functions[i] = function;
		return (E task, int thread) -> {add(task, thread, i);};
	}
	
	private class ThreadQueue {
		private int[] function;
		private Object[] tasks;
		private int start, end;
		
		public ThreadQueue(int max) {
			function = new int[max];
			tasks = new Object[max];
			start = end = 0;
		}
		
		public boolean isEmpty() {return start == end;}
		public synchronized void add(int func, Object task) {
			function[end] = func;
			tasks[end] = task;
			if(++end >= function.length) end = 0;
		}
		
		public synchronized int get() {
			if(start == end) return -1;
			int i = start; 
			if(++start >= function.length) 
				start = 0; 
			return i;
		}
		@SuppressWarnings("unchecked")
		public void run() {
			int i = -1;
			while((i = get()) >= 0) {
				functions[function[i]].accept(tasks[i]);
			}
		}
	}
}
