package com.oreilly.permitme.data;

import com.oreilly.permitme.PermitMe;

public enum PermitPriceDecayMethod {
	TIME, OTHERPERMIT;
	
	public static PermitPriceDecayMethod fromString( String data, String errorLocation ) {
		data = data.toLowerCase().trim();
		if ( data.contentEquals("time")) return TIME;
		if ( data.contentEquals("other")) return OTHERPERMIT;
		if ( data.contentEquals("other permit sold")) return OTHERPERMIT;
		PermitMe.log.warning( "[PermitMe] Error parsing pricing decay method from " + errorLocation + ". Data provided was " + data );
		return TIME;
	}
	
	@Override
	public String toString() {
		switch( this ) {
		case TIME: return "time";
		case OTHERPERMIT: return "other permit sold";
		}
		return null;
	}
}
