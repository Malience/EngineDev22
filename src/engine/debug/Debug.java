package engine.debug;

import static engine.debug.Severity.*;

public class Debug {
	private static final String GENERAL = "GENERAL";
	private static boolean timestamp = true;
	private static boolean showthread = true;
	private static boolean showseverity = true;
	private static boolean showtype = true; 
	private static Log console = new Log(System.out);
	//private static Log err = new Log(System.err);
	
	public static void trace(String message) {log(TRACE, message);}
	public static void debug(String message) {log(DEBUG, message);}
	public static void info(String message) {log(INFO, message);}
	public static void warn(String message) {log(WARN, message);}
	public static void error(String message) {log(ERROR, message);}
	public static void critical(String message) {log(CRITICAL, message);}
	public static void log(String message) {log(INFO, message);}
	
	public static void trace(String type, String message) {log(type, TRACE, message);}
	public static void debug(String type, String message) {log(type, DEBUG, message);}
	public static void info(String type, String message) {log(type, INFO, message);}
	public static void warn(String type, String message) {log(type, WARN, message);}
	public static void error(String type, String message) {log(type, ERROR, message);}
	public static void critical(String type, String message) {log(type, CRITICAL, message);}
	public static void log(String type, String message) {log(type, INFO, message);}
	
	public static void log(Severity severity, String message) {log(GENERAL, severity, message);}
	public static void log(String type, Severity severity, String message) {
		String time = timestamp ? "[" + java.time.LocalTime.now().toString().substring(0, 8) + "] " : "";
		String thread = showthread ? "[" + Thread.currentThread().getName() + "] " : "";
		String sev = showseverity ? "[" + severity.toString() + "] " : "";
		String t = showtype ? "[" + type + "] " : "";
		String m = time + sev + t + thread + ": "  + message;
		console.log(m);
	}
	
	public static void trace(Exception e) {log(TRACE, e);}
	public static void debug(Exception e) {log(DEBUG, e);}
	public static void info(Exception e) {log(INFO, e);}
	public static void warn(Exception e) {log(WARN, e);}
	public static void error(Exception e) {log(ERROR, e);}
	public static void critical(Exception e) {log(CRITICAL, e);}
	
	public static void trace(String type, Exception e) {log(type, TRACE, e);}
	public static void debug(String type, Exception e) {log(type, DEBUG, e);}
	public static void info(String type, Exception e) {log(type, INFO, e);}
	public static void warn(String type, Exception e) {log(type, WARN, e);}
	public static void error(String type, Exception e) {log(type, ERROR, e);}
	public static void critical(String type, Exception e) {log(type, CRITICAL, e);}
	
	public static void log(Exception e) {log(INFO, e);}
	public static void log(Severity severity, Exception e) {log(GENERAL, severity, e);}
	public static void log(String type, Severity severity, Exception e) {
		log(type, severity, e.toString());
		e.printStackTrace(console.stream);
	}
	
}
