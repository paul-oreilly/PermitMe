package com.oreilly.permitme.record;

public class SavedPermit extends Permit {

	public String filename;
	
	
	public SavedPermit( String signName, String UUID, String filename ) {
		super( UUID, signName );
		this.filename = filename;
	}
	
	

}
