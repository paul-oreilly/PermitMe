package com.oreilly.permitme.data;

import com.oreilly.permitme.PermitMe;

public enum EDI {
	ENABLED, DISABLED, INHERITIED;
	
	public static EDI fromString( String data, String parseError ) {
		data = data.toLowerCase().trim();
		if ( data.contentEquals( "enabled" )) return ENABLED;
		if ( data.contains( "disabled" )) return DISABLED;
		if ( data.contains( "inheritied" )) return INHERITIED;
		if ( parseError != null ) PermitMe.log.warning( "[PermitMe] Parse error (should be one of: enabled, disabled, inheritied): " + parseError );
		return null;
	}
	
	@Override
	public String toString() {
		switch( this ) {
		case ENABLED: return "enabled";
		case DISABLED: return "disabled";
		case INHERITIED: return "inheritied";
		default: return null;
		}
	}
}
