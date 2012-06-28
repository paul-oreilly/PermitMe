package com.oreilly.permitme;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import com.oreilly.permitme.record.Permit;
import com.oreilly.permitme.record.SavedPermit;


public class Permits {
	
	public HashMap< String, Permit > permitsByUUID = new HashMap< String, Permit>();
	public HashMap< String, Permit > permitsByAlias = new HashMap< String, Permit >();
	
	
	private final HashMap< String, List< Permit >> queueForInheritence = new HashMap< String, List< Permit >>();
	private final List< Permit > needUUIDs = new LinkedList< Permit >();
	
	
	public Permits() {
	}

	
	
	public void ConfigComplete() {
		for ( Permit permit : needUUIDs ) {
			String permitUUID = permit.name;
			int i = 2;
			boolean success = false;
			while ( ! success ) {
				if ( permitsByUUID.containsKey( permitUUID )) {
					permitUUID = permit.name + i;
					i += 1;
				} else success = true;
			}
			permit.UUID = permitUUID;
			permitsByUUID.put( permitUUID, permit );
			if ( permit instanceof SavedPermit )
				Config.savePermit( (SavedPermit)permit );
		}
		for ( Permit permit : permitsByUUID.values())
			permitsByAlias.put( permit.UUID, permit );
	}
	
	
	public void addAlias( Permit permit, String alias ) {
		// TODO: Uniqueness check + error messages
		permitsByAlias.put( alias, permit );
	}

	
	
	public void addPermit(Permit permit) {
		// TODO: Error message
		if ( permit == null ) return;
		// add to records
		if ( permit.UUID == null)
			needUUIDs.add( permit );
		else if (permit.UUID.length() == 0 )
			needUUIDs.add( permit );
		else { 
			if ( permitsByUUID.containsKey( permit.UUID ))
				PermitMe.log.warning("[PermitMe] !! UUID conflict - multiple " + permit.UUID + " values");
			else permitsByUUID.put( permit.UUID, permit );
		}

		resolveInheritence( permit );
	}
	
	
	private void resolveInheritence( Permit permit ) {
		Permit lookup;
		// add any permits that have already loaded
		for ( String name : permit.inheritenceAsStrings ) {
			lookup = permitsByUUID.get( name );
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
		for ( Permit permit : permitsByUUID.values())
			if ( permit instanceof SavedPermit )
				Config.savePermit( (SavedPermit)permit );
	}

	
	
}
