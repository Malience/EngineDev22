package engine.io;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;

public class IO {
	public static ByteBuffer loadShader(String path) {
		try {
			FileInputStream fis = new FileInputStream(path);
			FileChannel fc = fis.getChannel();
			ByteBuffer code = fc.map(MapMode.READ_ONLY, 0, fis.available());
			fc.close();
			fis.close();
			return code;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
}
