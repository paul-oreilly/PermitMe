package com.oreilly.permitme.record;

import java.util.HashSet;



public class PermitPlayer {

	public String name;
	public HashSet< String > permits = new HashSet< String >();
	
	public PermitPlayer( String name ) {
		this.name = name;
	}
	
	
	@Override
	public String toString() {
		return name;
	}
	
	
	public HashSet< String > getPermits() {
		HashSet< String > result = new HashSet< String >();
		result.addAll( permits );
		return result;
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

