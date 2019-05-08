package game;

import java.util.Arrays;
import java.util.Comparator;

import engine.Systems;
import engine.core.CoreEngine;
import engine.core.Time;
import engine.input.InputEngine;
import engine.rendering.RenderingEngine;
import engine.rendering.VulkanRenderingEngine;
import engine.rendering.VulkanRenderingEngine2;
import engine.rendering.VulkanRenderingEngine3;

public class VulkanTest {
	public static RenderingEngine rendering;
	public static CoreEngine core;
	public static InputEngine input;
	
	public static void main(String [] args) {
		org.lwjgl.system.Configuration.STACK_SIZE.set(100000);
		Systems.system_init(10);
		
		input = new InputEngine();
		rendering = new VulkanRenderingEngine3();
		
		core = new CoreEngine(input, rendering);
		Time.setFPS(60);
		core.start();
		core.run();
		
		Systems.terminate();
	}
}
