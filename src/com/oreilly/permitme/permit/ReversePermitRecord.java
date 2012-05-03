package com.oreilly.permitme.permit;

import java.util.LinkedList;
import java.util.List;
import java.util.TreeMap;

public class ReversePermitRecord extends TreeMap< Integer, List< String >>{

	private static final long serialVersionUID = 1L;

	public void addRecord( int id, String name ) {
		List< String > record = this.get( id );
		if ( record == null ) {
			record = new LinkedList< String >();
			this.put( id, record );
		}
		if ( record.contains( name ) == false ) record.add( name );		
	}

}
