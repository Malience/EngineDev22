package engine.objects;

import java.util.HashMap;
import java.util.HashSet;

public class ArrayObjectTable {
	private static final HashSet<Long> ids;
	private static final HashMap<Long, GameObject> objects;
	private static long next = 1L;
	
	static {
		ids = new HashSet<>();
		objects = new HashMap<>();
	}
	
	public static long create() {
		long id, start = next - 1;
		while(ids.contains(id = next++) && id != start);
		if(id == start) System.err.println("GameObject Table Overflow!");
		return id;
	}
	
}
