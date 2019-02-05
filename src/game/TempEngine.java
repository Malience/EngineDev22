package game;

import api.InputAPI;
import engine.Engine;
import engine.input.Input;

public class TempEngine extends Engine {

	protected TempEngine(String name) {
		super(name);
		// TODO Auto-generated constructor stub
	}
	
	public void run() {
		if(Input.getRange("Jump") > 0) System.out.println("It works!" + Input.getRange("Jump"));
	}

}
