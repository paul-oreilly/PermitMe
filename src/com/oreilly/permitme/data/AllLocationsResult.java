package com.oreilly.permitme.data;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class AllLocationsResult extends HashMap< String, AllLocationsListNode > {
	// data storage is "world name" -> "location name" -> result

	private static final long serialVersionUID = 1L;

	
	public void addRecord( String worldName, String locationName, String parent ) {
		addRecordWorker( worldName, locationName, parent, null );
	}
	
	
	public void addRecord( String worldName, String locationName, String parent, String[] children ) {
		if ( children != null ) 
			addRecordWorker( worldName, locationName, parent, Arrays.asList( children ));
		else
			addRecordWorker( worldName, locationName, parent, null );
	}
	
	
	public void addRecord( String worldName, String locationName, String parent, List< String > children ) {
		addRecordWorker( worldName, locationName, parent, children );
	}
	
	
	private void addRecordWorker( String worldName, String locationName, String parent, List< String > children ) {
		// check that world record exists, and location name is unique
		AllLocationsListNode location = this.get( locationName );
		if ( location == null ) {
			// add the data
			this.put( locationName, new AllLocationsListNode( locationName, parent, children, worldName ));
		} else {
			// TODO: Error message
		}
	}
	
}
