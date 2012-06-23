package com.oreilly.permitme.data;

import java.util.Collection;
import java.util.HashSet;
import java.util.TreeMap;

@SuppressWarnings("serial")
class RCPR_PermitListing extends HashSet< String > {}
@SuppressWarnings("serial")
class RCPR_DataListing extends TreeMap< Integer, RCPR_PermitListing > {}
@SuppressWarnings("serial")
class RCPR_IDListing extends TreeMap< Integer, RCPR_DataListing > {}



public class ReverseComplexPermitRecord extends RCPR_IDListing {

	private static final long serialVersionUID = 1L;

	public void addRecord( int id, int data, String alias ) {
		RCPR_DataListing dataListing = this.get( id );
		if ( dataListing == null ) {
			dataListing = new RCPR_DataListing();
			this.put( id, dataListing );
		}
		RCPR_PermitListing permitListing = dataListing.get( data );
		if ( permitListing == null ) {
			permitListing = new RCPR_PermitListing();
			dataListing.put( data, permitListing );
		}
		permitListing.add( alias );		
	}
	
	
	public Collection< String > getRecords( int id, int data ) {
		RCPR_DataListing dataListing = this.get( id );
		if ( dataListing == null ) return null;
		return dataListing.get( data );
	}
	
	
	public void addAll( ReverseComplexPermitRecord obj ) {
		if ( obj == null ) return;
		for ( Integer id : obj.keySet()) {
			RCPR_DataListing objDatas = obj.get( id );
			RCPR_DataListing selfDatas = this.get( id );
			if ( selfDatas == null ) { 
				selfDatas = new RCPR_DataListing();
				this.put( id, selfDatas );
			}
			if ( objDatas != null ) {
				for ( Integer data : objDatas.keySet()) {
					RCPR_PermitListing objPermits = objDatas.get( data );
					RCPR_PermitListing selfPermits = selfDatas.get( data );
					if ( selfPermits == null ) { 
						selfPermits = new RCPR_PermitListing();
						selfDatas.put( data, selfPermits );
					}
					if ( objPermits != null )
						selfPermits.addAll( objPermits );
				}
			}
		}
	}
}
