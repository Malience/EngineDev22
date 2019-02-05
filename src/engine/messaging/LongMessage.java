package engine.messaging;

public class LongMessage extends Message {
	public final long value;
	
	public LongMessage(String message, long value) {this(0, message, value);}
	public LongMessage(int thread, String message, long value) {
		super(thread, message);
		this.value = value;
	}
	
	public static LongMessage create(String message, long value) {return new LongMessage(message, value);}
	public static LongMessage create(int thread, String message, long value) {return new LongMessage(thread, message, value);}
}
