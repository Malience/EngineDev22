package engine.messaging;

@FunctionalInterface
public interface MessageHandler {
	public void handle(Message m);
}
