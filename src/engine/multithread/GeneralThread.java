package engine.multithread;

import static engine.multithread.ThreadState.*;

class GeneralThread implements PoolThread {
	private Thread thread;
	private ThreadPool pool;
	private ThreadState state;
	
	GeneralThread(ThreadPool pool, ThreadGroup group, String name){
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
				if(!pool.isEmpty(Threads.RES)) state = RESOURCE;
				if(!pool.isEmpty(Threads.ANY)) state = GENERAL;
				break;
			case RESOURCE: //Grabs a Resource Task from the pool
				pool.run(Threads.RES);
				state = FREE;
				break;
			case GENERAL: //Grabs a General Task from the pool
				pool.run(Threads.ANY);
				state = FREE;
				break;
			case SYNC:
				while(pool.getSync()) {Thread.sleep(1);}
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
