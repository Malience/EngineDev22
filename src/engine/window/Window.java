package engine.window;

import static api.InputAPI.*;

import org.lwjgl.glfw.GLFW;

import api.InputAPI;
import engine.debug.Debug;

public class Window {
	private long window;
	private int width, height;
	private String title;
	private static int vsync = 0;
	
	public long getHandle(){return window;}
	
	public Window(String title, int width, int height) {this(title, width, height, 0L, 0L);}
	public Window(String title, int width, int height, Window share) {this(title, width, height, 0L, share.window);}
	public Window(String title, int width, int height, long monitor, long share) {
		this.width = width; this.height = height; this.title = title;
				
		defaultWindowHints();
		windowHint(GLFW.GLFW_CLIENT_API, GLFW.GLFW_NO_API);
		
		window = createWindow(width, height, title, monitor, share);
		
		Debug.log("WINDOW", "Window created: " + title);
			
		//makeContextCurrent(window);
		//swapInterval(vsync);		
	}
	
	public void setCursorPos(float x, float y){InputAPI.setCursorPos(window, x, y);}
	public void setCursorMiddle() {InputAPI.setCursorPos(window, width >> 1, height >> 1);}
	public void lockCursor() {disableCursor(window);}
	public void unlockCursor() {enableCursor(window);}
	
	
	public void swapBuffers(){InputAPI.swapBuffers(this.window);}
	
	public boolean shouldClose() {return windowShouldClose(window);}
	
	public void dispose() {
		Debug.log("WINDOW", "Disposing of Window!");
		destroyWindow(window);
	}
	
	public void show() {showWindow(window);}
	public void hide() {hideWindow(window);}
	
	public int getWidth(){return width;}
	public int getHeight(){return height;}
	public String getTitle(){return title;}
}
