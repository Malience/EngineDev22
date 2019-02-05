package engine.input;

import java.util.HashMap;
import java.util.HashSet;

import api.InputAPI;

public class Input {
	private static Input input;
	private boolean[] press;
	private boolean[] hold;
	private boolean[] release;
	private HashSet<String> actions;
	private HashSet<String> states;
	private HashMap<String, Float> ranges;
	
	private Input() {
		press = new boolean[512];
		hold = new boolean[512];
		release = new boolean[512];
		actions = new HashSet<>();
		states = new HashSet<>();
		ranges = new HashMap<>();
	}
	
	static void init() {
		input = new Input();
	}
	
	public static boolean initialized() {
		return input != null;
	}
	
	static void dispose() {
		input = null;
	}
	
	static void reset() {
		for(int i = 0; i < input.press.length; i++) {
			input.press[i] = false;
			input.hold[i] = false;
			input.release[i] = false;
		}
		input.actions.clear();
		input.ranges.clear();
	}
	
	static void key(int key, int state) {
		if(input == null) return;
		switch(state) {
		case InputAPI.PRESS: input.press[key] = true; break;
		case InputAPI.HOLD: input.hold[key] = true; break;
		case InputAPI.RELEASE: input.release[key] = true; break;
		}
	}
	
	static void action(String action) {
		if(input == null) return;
		input.actions.add(action);
	}
	
	static void state(String action, int state) {
		if(input == null) return;
		if(state == InputAPI.PRESS) input.states.add(action);
		else if(state == InputAPI.RELEASE) input.states.remove(action);
	}
	
	static void range(String range, float value) {
		if(input == null) return;
		input.ranges.put(range, value);
	}
	
	public static boolean getKeyPressed(int key) {
		if(input == null) return false;
		return input.press[key];
	}
	
	public static boolean getKeyHeld(int key) {
		if(input == null) return false;
		return input.hold[key];
	}
	
	public static boolean getKeyReleased(int key) {
		if(input == null) return false;
		return input.release[key];
	}
	
	public static boolean getAction(String action) {
		if(input == null) return false;
		return input.actions.contains(action);
	}
	
	public static boolean getState(String state) {
		if(input == null) return false;
		return input.states.contains(state);
	}
	
	public static float getRange(String range) {
		if(input == null || !input.ranges.containsKey(range)) return 0f;
		return input.ranges.get(range);
	}
}
