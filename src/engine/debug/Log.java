package engine.debug;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;

import static engine.io.Pathss.LOG_DIR;

class Log {
	public final PrintStream stream;
	Log(String filename) {this(new File(LOG_DIR + filename));}
	Log(File file) {
		PrintStream stream = null;
		try {
			stream = new PrintStream(file);
		} catch (FileNotFoundException e) {
			Debug.log(Severity.ERROR, "Log Error - File not found: " + file.getPath());
			Debug.log(Severity.ERROR, e.toString());
		}
		this.stream = stream;
	}
	Log(PrintStream stream) {this.stream = stream;}
	void log(String message) {stream.println(message);};
}
