package engine;

import java.util.HashMap;

public class OptionsSystem extends SubSystem {
	private static OptionsSystem system;
	private HashMap<String, Options> options;
	
	public OptionsSystem() {
		super("Options");
		options = new HashMap<>();
		system = this;
	}
	
	@Override
	public void init() {
		for(Options option : options.values()) option.load();
		//TODO: Fix
//		MessageSystem.createEndpoint("Options", (Message m) -> {
//			if(m instanceof IOMessage) {
//				IOMessage io = (IOMessage) m;
//				Options o;
//				switch(io.code) {
//				case LOAD: 
//					o = options.get(io.file);
//					if(o == null) options.put(io.file, (o = new Options(io.file)));
//					o.load();
//					return;
//				case SAVE:
//					o = options.get(io.file);
//					if(o != null) o.save();
//					return;
//				}
//			}
//		});
	}
	@Override
	public void terminate() {for(Options option : options.values()) option.save();}
	
	public static boolean initialized() {return system != null;}
	public static Options getOptions(String name) {return system.options.get(name);}
}
