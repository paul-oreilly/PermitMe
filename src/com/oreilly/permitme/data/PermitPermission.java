package com.oreilly.permitme.data;

public enum PermitPermission {
	UNDEFINED, GRANTED, DENIED;
	
	public static PermitPermission fromString( String name ) {
		name = name.toLowerCase();
		if ( name == "true" ) return PermitPermission.GRANTED;
		if ( name == "false" ) return PermitPermission.DENIED;
		return PermitPermission.UNDEFINED;
	}
	
	@Override
	public String toString() {
		switch( this ) {
		case GRANTED: return "True";
		case DENIED: return "False";
		default: return "Undefined";
		}
	}

}