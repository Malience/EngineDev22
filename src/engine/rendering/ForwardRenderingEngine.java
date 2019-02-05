package engine.rendering;

import engine.messaging.Message;

public class ForwardRenderingEngine extends RenderingEngine {
	public ForwardRenderingEngine() {super();}

	@Override
	public void start() {
		super.start();
		Message.immediate("Show").post();
		swap = Message.immediate("Swap").setDest("Input");
	}

	Message swap;
	
	@Override
	public void run() {
		swap.post();
	}

	@Override
	public void dispose() {
		super.dispose();
	}
}
