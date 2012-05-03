package com.oreilly.permitme.player;

import java.util.LinkedList;
import java.util.List;



public class PermitPlayer {

	//public HashMap< String, PlayerPermitInfo > permits = new HashMap< String, PlayerPermitInfo >();
	public String name;
	public List< String > permits = new LinkedList< String >();
	
	public PermitPlayer( String name ) {
		this.name = name;
	}
	
	/*public void addPermit( String name, int fee, int daysRemaining ) {
		permits.put( name, new PlayerPermitInfo( name, fee, daysRemaining ));
	} */

	
}

