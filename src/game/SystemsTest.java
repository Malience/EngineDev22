package game;

import engine.OptionsSystem;
import engine.SystemMessage;
import engine.Systems;
import engine.core.CoreEngine;
import engine.core.Time;
import engine.input.InputEngine;
import engine.multithread.ThreadSystem;
import engine.rendering.ForwardRenderingEngine;
import engine.rendering.RenderingEngine;
import engine.window.WindowSystem;

public class SystemsTest {
	public static InputEngine input;
	public static RenderingEngine rendering;
	public static CoreEngine core;
	
	public static void main(String [] args) {
		Systems.system_init(10);	
		
		SystemMessage.instantiate(ThreadSystem.class).post();
		SystemMessage.instantiate(OptionsSystem.class).post();
		SystemMessage.instantiate(WindowSystem.class).post();
		
		SystemMessage.init("Thread").post();
		SystemMessage.init("Options").post();
		SystemMessage.init("Window").post();
		
		input = new InputEngine();
		rendering = new ForwardRenderingEngine();
		core = new CoreEngine(input, rendering, new TempEngine("Temp"));
		Time.setFPS(0);
		core.start();
		//core.run();
		
		Systems.terminate();
	}
}
