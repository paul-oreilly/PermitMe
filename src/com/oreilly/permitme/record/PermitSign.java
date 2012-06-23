package com.oreilly.permitme.record;

import org.bukkit.Location;
import org.bukkit.World;

import com.oreilly.permitme.PermitMe;


public class PermitSign {
	public Location location;
	public Permit permit;

	
	static public PermitSign fromString( String world, String data ) {
		// save format is "permitUUID @ x y z"
		// TODO: Error messages
		if ( data.length() < 9 ) return null;
		String[] split = data.split("@");
		if ( split.length < 2 ) return null;
		String permitName = split[0].trim();
		split = split[1].split(" ");
		if ( split.length < 3 ) return null;
		// TODO Error catching
		int x = Integer.parseInt( split[0]);
		int y = Integer.parseInt( split[1]);
		int z = Integer.parseInt( split[2]);
		//TODO Null checks
		World w = PermitMe.instance.getServer().getWorld( world );
		Location location = new Location( w, x, y, z );
		Permit permit = PermitMe.instance.permits.permitsByUUID.get( permitName );
		// return new sign record
		if (( location != null ) & ( permit != null ))
			return new PermitSign( location, permit );
		else return null;
	}
	
	
	public PermitSign(Location location, Permit permit) {
		this.location = location;
		this.permit = permit;
	}
	
	
	@Override
	public String toString() {
		// save format is "permitUUID @ x y z"
		return ( permit.signName + " @ " + location.getBlockX() + " " + location.getBlockY() + " " + location.getBlockZ());
	}
	
	
}
