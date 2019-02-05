package engine.input;

import engine.messaging.Message;

public class StateMessage extends Message {
	public final int state;
	public StateMessage(String action, int state) {
		super(0, action);
		this.state = state;
	}
	
}
