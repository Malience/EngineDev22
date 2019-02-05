package engine;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Properties;

import engine.debug.Debug;
import engine.io.Pathss;

public class Options {
	private String name;
	private Properties prop;
	
	public Options(String filename) {name = filename; prop = new Properties(); load();}
	
	public void load() {load(name);}
	public void load(String filename) {
		try {
			FileInputStream fis = new FileInputStream(Pathss.CONFIG_DIR + filename + Pathss.CONFIG_EXTENSION);
			prop.load(fis);
			fis.close();
		} catch (FileNotFoundException e) {
			Debug.error("IO", e);
		} catch (IOException e) {
			Debug.error("IO", e);
		}
	}
	
	public void save() {save(name);}
	public void save(String filename) {
		try {
			FileOutputStream fos = new FileOutputStream(Pathss.CONFIG_DIR + filename + Pathss.CONFIG_EXTENSION);
			prop.store(fos, null);
			fos.close();
		} catch (FileNotFoundException e) {
			Debug.error("IO", e);
		} catch (IOException e) {
			Debug.error("IO", e);
		}
	}
	
	public boolean getBoolean(String option) 		{return Boolean.parseBoolean(prop.getProperty(option));}
	public int getInt(String option) 				{return Integer.parseInt(prop.getProperty(option));}
	public float getFloat(String option) 			{return Float.parseFloat(prop.getProperty(option));}
	public long getLong(String option) 				{return Long.parseLong(prop.getProperty(option));}
	public String getString(String option) 			{return prop.getProperty(option);}
	
	public void set(String option, boolean value) 	{prop.put(option, value);}
	public void set(String option, int value) 		{prop.put(option, value);}
	public void set(String option, float value) 	{prop.put(option, value);}
	public void set(String option, long value) 		{prop.put(option, value);}
	public void set(String option, String value) 	{prop.put(option, value);}
	
	public void print() {print(System.out);}
	public void print(PrintStream out) {prop.list(out);}
}
