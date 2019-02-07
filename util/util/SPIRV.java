package util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.nio.file.Files;
import java.nio.file.Paths;

import engine.debug.Debug;

public class SPIRV {
	private static final int VERT = 0x1, FRAG = 0x2, GEOM = 0x4, COMP = 0x3;
	
	private static final String SDK_HOME = "E:/VulkanSDK/1.1.82.1/Bin32";
	private static final String GLSLANG_VALIDATOR = SDK_HOME + "/glslangValidator.exe";
	private static final String SHADER_PATH = "./res/shaders/";
	private static final String TEMP_PATH = "./temp/";
	
	private static final String FORMAT = "\"" + GLSLANG_VALIDATOR + "\" -V -o \"%s\" \"%s\"";
	
	public static ByteBuffer compileFile(String filename) {
		String src = SHADER_PATH + filename;
		String dst = TEMP_PATH + filename + ".spv";
		if(!Files.isRegularFile(Paths.get(src))) {Debug.error("IO", "Shader file not found, could not compile: " + src); return null;}
		ByteBuffer code = null;
		try {
			Process p = new ProcessBuilder(String.format(FORMAT, dst, src)).inheritIO().start();
			p.waitFor();
			p.destroy();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			FileInputStream fis = new FileInputStream(dst);
			FileChannel fc = fis.getChannel();
			code = fc.map(MapMode.READ_ONLY, 0, fis.available());
			fc.close();
			fis.close();
			new File(dst).delete();
		} catch(Exception e) {
			
		}
		return code;
	}
	
	public static void program(String filename) {
		try {
			BufferedReader br = new BufferedReader(new FileReader(new File(filename)));
			int type = 0x0;
			String line, shader = "";
			while(br.ready()) {
				line = br.readLine();
				if(line.length() > 0 && line.charAt(0) == '@') {
					String path = TEMP_PATH + filename + "." + filetype(type);
					//Compile
					switch(line.toUpperCase().trim()) {
					case "@VERTEX": type = VERT; break;
					case "@GEOMETRY": type = GEOM; break;
					case "@FRAGMENT": type = FRAG; break;
					case "@COMPUTE": type = COMP; break;
					default: Debug.error("SHADER", "Unsupported Shader Type: " + line); return;
					}
					shader = "";
				} else shader += line + "\n";
			}
			//Compile
			br.close();
		} catch(Exception e) {
			
		}
	}
	
	public static void compile(String shader, int type) {
		
	}
	
	public static String filetype(int type) {
		switch(type) {
		case VERT: return "vert";
		case GEOM: return "geom";
		case FRAG: return "frag";
		case COMP: return "comp";
		}
		return null;
	}
}
