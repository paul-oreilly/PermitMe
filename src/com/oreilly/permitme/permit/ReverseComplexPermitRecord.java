package com.oreilly.permitme.permit;

import java.util.LinkedList;
import java.util.List;
import java.util.TreeMap;

public class ReverseComplexPermitRecord extends TreeMap< Integer, TreeMap< Integer, LinkedList< String >>>{

	private static final long serialVersionUID = 1L;

	public void addRecord( int id, int data, String name ) {
		TreeMap<Integer, LinkedList<String>> record = this.get( id );
		if ( record == null ) {
			record = new TreeMap< Integer, LinkedList< String >>();
			this.put( id, record );
		}
		LinkedList< String > dataRecord = record.get( data );
		if ( dataRecord == null ) {
			dataRecord = new LinkedList< String >();
			record.put( data, dataRecord );
		}
		if ( dataRecord.contains( name ) == false ) dataRecord.add( name );		
	}
	
	
	public List< String > getRecords( int id, int data ) {
		TreeMap<Integer, LinkedList<String>> record = this.get( id );
		if ( record == null ) return null;
		return record.get( data );
	}

}
