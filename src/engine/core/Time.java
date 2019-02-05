package engine.core;


public abstract class Time {
	private static final long SECOND = 1000000000L;
	private static final float INV_SECOND = 1.0f / (float) SECOND;
	//private static final long MILLISECOND = 1000000L;
	//private static final float INV_MILLISECOND = 1.0f / (float) MILLISECOND;
	
	private static long frame = 0L;
	
	private static int frames, lastFrames, FPS;
	private static float lastFrame, deltaFrame, lastTime, deltaTime, invFPS, unprocessedTime, frameCounter;
	
	static { lastFrame = lastTime = getTime(); FPS = 60; invFPS = 1f/FPS;}
	
	public static float getDelta() { return deltaFrame; }
	public static float getTime() { return System.nanoTime() * INV_SECOND; }
	public static float getMilliTime() { return System.currentTimeMillis(); }
	public static float getNanoTime() { return System.nanoTime();}
	
	public static void setFPS(int fps) {FPS = fps; invFPS = fps <= 0 ?  0 : 1f/fps;}
	
	public static boolean processFrame() {
		float time = getTime();
		deltaTime = time - lastTime;
		lastTime = time;
		unprocessedTime += deltaTime;
		frameCounter += deltaTime;
		if(unprocessedTime >= invFPS) {
			unprocessedTime = FPS == 0 ? 0 : unprocessedTime % invFPS;
			if(frameCounter >= 1.0) {
				lastFrames = frames; 
				frameCounter = frames = 0;
				System.out.println(lastFrames);
			}
			deltaFrame = time - lastFrame;
			lastFrame = time;
			frames++;
			frame++;
			return true;
		}
		return false;
	}
	
	public static long getFrame() {return frame;}
	public static int getFrames() {return lastFrames;}
}
