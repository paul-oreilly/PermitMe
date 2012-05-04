package com.oreilly.permitme.data;

import com.oreilly.permitme.PermitMe;

public enum PermitPricingMethod {
	SIMPLE, FACTOR, RATIO;
	
	public static PermitPricingMethod fromString( String data, String errorLocation ) {
		data = data.toLowerCase().trim();
		if ( data.contentEquals( "simple" )) return SIMPLE;
		if ( data.contentEquals( "factor" )) return FACTOR;
		if ( data.contentEquals( "ratio" )) return RATIO;
		PermitMe.log.warning( "[PermitMe] Error parsing pricing method from " + errorLocation + ". Data provided was " + data );
		return SIMPLE;
	}
	
	@Override
	public String toString() {
		switch( this ) {
		case SIMPLE: return "simple";
		case FACTOR: return "factor";
		case RATIO: return "ratio";
		}
		return null;
	}
}
