package engine.debug;

enum Severity {
	TRACE,		//For tracing code and finding something specific
	DEBUG,		//Debug information
	INFO,		//General information
	WARN,		//Something that could cause an issue but is currently accounted for
	ERROR,		//The current operation could not be completed properly
	CRITICAL; 	//The program cannot continue and must shut down
	
	public String toString() {
		switch(this) {
		case TRACE: 	return "TRACE";
		case DEBUG: 	return "DEBUG";
		case INFO: 		return "INFO";
		case WARN: 		return "WARN";
		case ERROR: 	return "ERROR";
		case CRITICAL: 	return "CRITICAL";
		}
		return "";
	}
}
