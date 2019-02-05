package engine.messaging;

public class RequestMessage extends Message {
	public final String request;
	
	public RequestMessage(String request) {this(0, request);}
	public RequestMessage(int thread, String request) {super(thread); this.request = request;}
	
}
