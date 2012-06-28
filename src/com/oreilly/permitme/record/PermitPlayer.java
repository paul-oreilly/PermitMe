package com.oreilly.permitme.record;

import java.util.LinkedList;
import java.util.List;



public class PermitPlayer {

	public String name;
	public List< String > permits = new LinkedList< String >();
	
	public PermitPlayer( String name ) {
		this.name = name;
	}
	
	
	@Override
	public String toString() {
		return name;
	}
	
	
	public String toHumanString() {
		String result = "Player " + name;
		if ( permits.size() == 0 )
			result += " (no permits)";
		else {
			result += " with " + permits.size() + " permits: ";
			for ( String permitName : permits )
				result += "\n  - " + permitName;
		}
		return result;
	}
	
}

