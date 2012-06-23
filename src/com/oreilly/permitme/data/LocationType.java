package com.oreilly.permitme.data;

import com.oreilly.permitme.PermitMe;




public class LocationType {
	
	public LocationTypeEnum overallType = LocationTypeEnum.UNIVERSAL;
	// detail field is used when type is "Area" to define what kind of area - eg residence, worldguard etc.
	//  eg: area(residence)
	public String detail = null;
	
	
	static public LocationType fromString( String data, String errorLocation ) {
		// expected input is "universal", "world", and "area([type])"
		data = data.toLowerCase().trim();
		if ( data.contentEquals( LocationTypeEnum.UNIVERSAL.toString())) 
			return new LocationType( LocationTypeEnum.UNIVERSAL, null );
		if ( data.contentEquals( LocationTypeEnum.WORLD.toString()))
			return new LocationType( LocationTypeEnum.WORLD, null );
		if ( data.startsWith("area")) {
			String detail = data.substring( data.indexOf("("), data.lastIndexOf(")") - 1 );
			return new LocationType( LocationTypeEnum.AREA, detail );
		}
		// if we havn't returned by this point, we are in error...
		PermitMe.log.warning("[PermitMe] Error parsing string to LocationType within " + errorLocation + ". Data is: " + data );
		return null;
	}
	
	
	public LocationType( LocationTypeEnum overall, String detail ) {
		this.overallType = overall;
		this.detail = detail;
	}
	
	
	@Override
	public String toString() {
		if ( overallType == LocationTypeEnum.AREA )
			return overallType.toString() + "(" + detail + ")";
		else return overallType.toString();
	}
	
}
