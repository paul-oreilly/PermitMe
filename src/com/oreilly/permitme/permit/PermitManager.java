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
import com.oreilly.permitme.data.BlockDataRecord;
import com.oreilly.permitme.data.ReverseComplexPermitRecord;
import com.oreilly.permitme.data.ReversePermitRecord;


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
	
	private final HashMap< String, List< Permit >> queueForInheritence = new HashMap< String, List< Permit >>();
	
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
		
		resolveInheritence( permit );
	}
	
	
	private void resolveInheritence( Permit permit ) {
		Permit lookup;
		// add any permits that have already loaded
		for ( String name : permit.inheritenceAsStrings ) {
			lookup = permits.get( name );
			if ( lookup != null ) permit.inherits.add( lookup );
			else permit.inheritencePending.add( name );
		}
		// for any remaining "willInherits", add this permit to the queueForInheritence
		for ( String name : permit.inheritencePending ) {
			if ( queueForInheritence.containsKey(name ) == false )
				queueForInheritence.put( name, new LinkedList< Permit >());
			queueForInheritence.get( name ).add( permit );
		}
		// see if any other permits were waiting to inherit from this one...
		if ( queueForInheritence.containsKey( permit.name )) {
			for ( Permit other : queueForInheritence.get( permit.name )) {
				other.inherits.add( permit );
				other.inheritencePending.remove( permit.name );
			}
		}
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
