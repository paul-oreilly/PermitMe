package com.oreilly.permitme.data;

import java.util.HashSet;
import java.util.TreeMap;

@SuppressWarnings("serial")
class RPR_PermitListing extends HashSet< String > {}
@SuppressWarnings("serial")
class RPR_IDListing extends TreeMap< Integer, RPR_PermitListing >{};


public class ReversePermitRecord extends RPR_IDListing {

	private static final long serialVersionUID = 1L;

	public void addRecord( int id, String alias ) {
		RPR_PermitListing record = this.get( id );
		if ( record == null ) {
			record = new RPR_PermitListing();
			this.put( id, record );
		}
		record.add( alias );		
	}
	
	
	public void addAll( ReversePermitRecord data ) {
		if ( data == null ) return;
		for ( Integer key : data.keySet()) {
			RPR_PermitListing target = this.get( key );
			if ( target == null ) {
				target = new RPR_PermitListing();
				this.put( key, target );
			}
			RPR_PermitListing dataPermits = data.get( key );
			if ( dataPermits != null )
				target.addAll( dataPermits );
		}
	}

}
