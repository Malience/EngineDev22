package game;

import engine.Systems;
import engine.core.CoreEngine;
import engine.core.Time;
import engine.input.InputEngine;
import engine.rendering.RenderingEngine;
import engine.rendering.VulkanRenderingEngine;

public class VulkanTest {
	public static RenderingEngine rendering;
	public static CoreEngine core;
	public static InputEngine input;
	
	public static void main(String [] args) {
		Systems.system_init(10);
		
		input = new InputEngine();
		rendering = new VulkanRenderingEngine();
		
		core = new CoreEngine(input, rendering);
		Time.setFPS(60);
		core.start();
		core.run();
		
		Systems.terminate();
	}
}
