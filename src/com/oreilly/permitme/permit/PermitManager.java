package com.oreilly.permitme.permit;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import com.oreilly.permitme.Config;
import com.oreilly.permitme.PermitMe;


public class PermitManager {
	
	public HashMap< String, Permit > permits = new HashMap< String, Permit>();

	// A reverse map from permits, from ID to a list of permits referencing that ID[:data]
	public ReversePermitRecord blockBreakingIndex = new ReversePermitRecord();
	public ReverseComplexPermitRecord blockBreakingComplexIndex = new ReverseComplexPermitRecord();
	public ReversePermitRecord blockPlacingIndex = new ReversePermitRecord();
	public ReverseComplexPermitRecord blockPlacingComplexIndex = new ReverseComplexPermitRecord();
	public ReversePermitRecord blockUseIndex = new ReversePermitRecord();
	public ReverseComplexPermitRecord blockUseComplexIndex = new ReverseComplexPermitRecord();
	public ReversePermitRecord itemUseIndex = new ReversePermitRecord();
	public ReverseComplexPermitRecord itemUseComplexIndex = new ReverseComplexPermitRecord();
	public ReversePermitRecord itemCraftingIndex = new ReversePermitRecord();
	public ReverseComplexPermitRecord itemCraftingComplexIndex = new ReverseComplexPermitRecord();
	
	// TODO: Records for enchanting, golems etc.
	
	// the main class
	//private PermitMe manager;

	
	public PermitManager( PermitMe manager ) {
	//	this.manager = manager;
	}
	
	
	public void addPermit(Permit permit) {
		permits.put( permit.name, permit );
		// add each set of information to the reverse lookups
		reverseIndexBasic( permit, permit.blockBreak, blockBreakingIndex );
		reverseIndexComplex( permit, permit.blockBreakMeta, blockBreakingComplexIndex );
		reverseIndexBasic( permit, permit.blockPlace, blockPlacingIndex );
		reverseIndexComplex( permit, permit.blockPlaceMeta, blockPlacingComplexIndex );
		reverseIndexBasic( permit, permit.blockUse, blockUseIndex );
		reverseIndexComplex( permit, permit.blockUseMeta, blockUseComplexIndex );
		reverseIndexBasic( permit, permit.itemUse, itemUseIndex );
		reverseIndexComplex( permit, permit.itemUseMeta, itemUseComplexIndex );
		reverseIndexBasic( permit, permit.crafting, itemCraftingIndex );
		reverseIndexComplex( permit, permit.craftingMeta, itemCraftingComplexIndex );
		// TODO: Other records
	}

	
	public void save() {
		for ( Permit permit : permits.values())
			Config.savePermit( permit );
	}

	
	public Set< String > getCombinedPermits( int id, int data, 
			ReversePermitRecord simple, ReverseComplexPermitRecord complex  ) {
			// used when a simple record and a metadata record need to be combined
		TreeSet< String > result = new TreeSet< String >();
		List<String> list = simple.get(id);
		if ( list != null )
			for ( String name : list )
				result.add( name );
		TreeMap<Integer, LinkedList<String>> record = complex.get( id );
		if ( record != null ) {
			list = record.get( data );
			if ( list != null )
				for ( String name : list )
					result.add( name );
		}
		return result;
	}	
	
	private void reverseIndexBasic( Permit permit, LinkedList<Integer> data, ReversePermitRecord target ) {
		Iterator< Integer > i = data.iterator();
		int id;
		while ( i.hasNext()) {
			id = i.next();
			target.addRecord( id, permit.name );
		}		
	}
	
	
	private void reverseIndexComplex( Permit permit, BlockDataRecord data, ReverseComplexPermitRecord target ) {
		Set< Integer > keyset = data.keySet();
		Iterator< Integer > iterID = keyset.iterator();
		Iterator< Integer > iterData;
		int id; int idata; List< Integer > list;
		while ( iterID.hasNext()) {
			id = iterID.next();
			list = data.get( id );
			if ( list == null ) continue;
			iterData = list.iterator();
			while ( iterData.hasNext()) {
				idata = iterData.next();
				target.addRecord( id, idata, permit.name );
			}
		}
	}
	
	
	
}
