package com.oreilly.permitme.data;

import java.util.HashMap;

public class LocationInstanceRecord<T> extends HashMap< String, HashMap< String, HashMap< String, T >>>{
	
	public static final long serialVersionUID = 1;
	
	public T getRecord( String key1, String key2, String key3 ) {
		HashMap< String, HashMap< String, T >> record1 = this.get( key1 );
		if ( record1 == null ) return null;
		HashMap< String, T > record2 = record1.get( key2 );
		if ( record2 == null ) return null;
		return record2.get( key3 );
	}
	
	
	public void addRecord( String key1, String key2, String key3, T value ) {
		HashMap< String, HashMap< String, T >> record1 = this.get( key1 );
		if ( record1 == null ) {
			record1 = new HashMap< String, HashMap< String, T >>();
			this.put( key1, record1 );
		}
		HashMap< String, T > record2 = record1.get( key2 );
		if ( record2 == null ) {
			record2 = new HashMap< String, T >();
			record1.put( key2, record2 );
		}
		record2.put( key3, value );
	}
}
