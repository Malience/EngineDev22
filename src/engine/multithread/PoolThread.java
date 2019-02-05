package engine.multithread;

interface PoolThread {
	void start();
	void run();
	void dispose();
	ThreadState getState();
}
