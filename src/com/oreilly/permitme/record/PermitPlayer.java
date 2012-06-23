package com.oreilly.permitme.record;

import java.util.LinkedList;
import java.util.List;



public class PermitPlayer {

	public String name;
	public List< String > permits = new LinkedList< String >();
	
	public PermitPlayer( String name ) {
		this.name = name;
	}
	
}

