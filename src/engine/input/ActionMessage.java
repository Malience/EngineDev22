package engine.input;

import engine.messaging.Message;

public class ActionMessage extends Message {

	public ActionMessage(String action) {
		super(0, action);
	}
	
}
