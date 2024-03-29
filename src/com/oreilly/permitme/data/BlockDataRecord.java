package com.oreilly.permitme.data;

import java.util.HashMap;
import java.util.LinkedList;

public class BlockDataRecord extends HashMap< Integer, LinkedList< Integer >>{
	private static final long serialVersionUID = 1L;
	
	public BlockDataRecord() {
		super();
	}
	
	public void addRecord( int id, int meta ) {
		if ( this.containsKey( id ) == false ) {
			this.put(id, new LinkedList< Integer>());
		}
		this.get(id).add(meta);
	}
	
	public boolean containsRecord( int id, int meta ) {
		LinkedList< Integer > list = this.get( id );
		if ( list == null ) return false;
		return list.contains( meta );
	}
}
