package engine.multithread;

public abstract class Threads {
	public static final int IMMEDIATE	= -2;
	public static final int SAME		= -1; //Must be on the same thread
	public static final int ANY			= 0x0; //Can be on any thread
	public static final int MAIN		= 0x1; //Must be on the main thread
	public static final int GPU			= 0x2; //Must be on the graphics thread
	public static final int RES			= 0x3; //Must be on the resource thread
	
	public static final int THREAD_TYPES = 0x4;
	
	private static final long MAIN_THREAD_ID = Thread.currentThread().getId();
	public static boolean isMainThread() {return Thread.currentThread().getId() == MAIN_THREAD_ID;}
}
