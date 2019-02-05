package engine.multithread;

import static engine.multithread.ThreadState.*;

class GPUThread implements PoolThread {
	private Thread thread;
	private ThreadPool pool;
	private ThreadState state;
	
	GPUThread(ThreadPool pool, ThreadGroup group, String name){
		thread = new Thread(group, this::run, name);
		this.pool = pool;
	}
	
	@Override
	public void start() {state = FREE; thread.start();}
	
	@Override
	public void run() {
		try {
		while(true) { //Loops after each task is completed
			if(state == TERMINATE) return;
			if(pool.getSync()) state = SYNC;
			switch(state) {
			case FREE:
				if(!pool.isEmpty(Threads.GPU)) state = GPU;
				if(!pool.isEmpty(Threads.ANY)) state = GENERAL;
				break;
			case GPU: //Grabs a gpu Task from the pool
				pool.run(Threads.GPU);
				state = FREE;
				break;
			case GENERAL: //Grabs a General Task from the pool
				pool.run(Threads.ANY);
				state = FREE;
				break;
			case SYNC:
				while(pool.getSync()) {}
				state = FREE;
				break;
			case TERMINATE:
			default:
				return;
			}
			Thread.sleep(1);
		}} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public ThreadState getState() {return state;}
	@Override
	public void dispose() {state = TERMINATE;}
}
