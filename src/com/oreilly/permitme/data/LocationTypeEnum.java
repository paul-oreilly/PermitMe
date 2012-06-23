package com.oreilly.permitme.data;

import com.oreilly.permitme.PermitMe;

public enum LocationTypeEnum {
	UNIVERSAL, WORLD, AREA;
	
	public static LocationTypeEnum fromString( String data, String errorLocation ) {
		data = data.toLowerCase().trim();
		if ( data.contentEquals( "world" )) return WORLD;
		if ( data.contentEquals( "area" )) return AREA;
		if ( data.contentEquals( "universal")) return UNIVERSAL;
		if ( data.contentEquals( "all")) return UNIVERSAL;
		PermitMe.log.warning( "[PermitMe] Parse error (should be one of: universal, world, residence, region): " + errorLocation );
		return null;
	}
	
	
	@Override
	public String toString() {
		switch ( this ) {
		case WORLD: return "world";
		case AREA: return "area";
		case UNIVERSAL: return "universal";
		default: return null;
		}
	}
}
