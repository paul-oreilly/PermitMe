package com.oreilly.permitme.permit;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class PermitRatio {

	public String target;
	public int parentNumber;
	public int targetNumber;


	static public List< PermitRatio > fromStrings( Collection< String > data ) {
		LinkedList< PermitRatio > result = new LinkedList< PermitRatio >();
		for ( String s : data )
			result.add( fromString( s ));
		return result;
	}
	
	
	static public PermitRatio fromString( String data ) {
		// Assumed string is "target, parentNumber:targetNumber"
		String[] split = data.split(",");
		if ( split.length == 0 ) return null;
		if ( split.length > 1 ) return null; 
		String target = split[0].trim();
		split = split[1].split(":");
		int parentNumber = Integer.parseInt( split[0].trim());
		int targetNumber = Integer.parseInt( split[1].trim());
		return new PermitRatio( target, parentNumber, targetNumber );
	}
	
	
	static public PermitRatio example() {
		return new PermitRatio( "Another perimt", 5, 2 );
	}
	
	
	public PermitRatio( String target, int parentNumber, int targetNumber ) {
		this.target = target;
		this.parentNumber = parentNumber;
		this.targetNumber = targetNumber;
	}	

	@Override
	public String toString() {
		return target + ", " + parentNumber + ":" + targetNumber;
	}
}
