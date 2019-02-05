package engine.input;

import engine.messaging.Message;

public class RangeMessage extends Message {
	public final float range;
	public RangeMessage(String action, float range) {
		super(0, action);
		this.range = range;
	}
	
}
