package com.oreilly.permitme.data;

import com.oreilly.permitme.PermitMe;

// True, false, or undefined

public enum TFU {
	TRUE, FALSE, UNDEFINED;
	
	public static TFU fromString( String data, String errorLocation ) {
		data = data.toLowerCase().trim();
		if ( data.contentEquals( "true" )) return TRUE;
		if ( data.contains( "false" )) return FALSE;
		if ( data.contains( "undefined" )) return UNDEFINED;
		if ( errorLocation != null ) PermitMe.log.warning( "[PermitMe] Parse error (should be true or false): " + errorLocation );
		return null;
	}
	
	
	public static TFU fromBoolean( Boolean data ) {
		if ( data ) return TRUE;
		else return FALSE;
	}
	
	
	@Override
	public String toString() {
		switch (this) {
		case TRUE: return "true";
		case FALSE: return "false";
		case UNDEFINED: return "undefined";
		default: return "unknown";
		}
	}
	
	
	public boolean toBoolean() {
		if ( this == TRUE ) return true;
		else return false;
	}
}
