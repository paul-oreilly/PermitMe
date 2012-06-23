package com.oreilly.permitme.data;

import java.util.HashMap;
import java.util.LinkedList;

import com.oreilly.permitme.PermitMe;
import com.oreilly.permitme.record.LocationRecord;
import com.oreilly.permitme.record.LocationTemplate;
import com.oreilly.permitme.record.Permit;

// TODO: Class

// One of these will be instanced for every world, residence, region etc in the game.

public class LocationInstance {

	
	public String name = null;
	public String locationType = null;
	public String world = null;
	
	public boolean isolation = false;
	public boolean strictMode = false;			// Implement later
	public boolean singleRole = false;
	//public boolean forgetOnDeath = false;
	//public long forgetCooldownTime = 86400;  // 24hrs, in seconds

	// what gets appended to permit names? (Isolated area inheritance support)
	public String isolationPrefix = "";	
	
	// parent, tracked to build the isolationPrefix when required 
	public LocationInstance parent = null;
	
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
	
	public HashMap< String, Permit > permitsByAlias = new HashMap< String, Permit >();
	public HashMap< String, Permit > permitsBySignName = new HashMap< String, Permit >();
	public HashMap< String, Permit > permitsByUID = new HashMap< String, Permit >();
	
	
	public LocationInstance( String name ) {
		this.name = name;
	}
	
	
	public LocationInstance( String name, LocationInstance parent ) {
		this( name, parent, null, null );
	}
	
	
	public LocationInstance( String name, LocationInstance parent, String locationType, String world ) {
		this.name = name;
		this.parent = ( parent == PermitMe.instance.locations.defaultLocationInstance ) ? null : parent;
		this.locationType = ( locationType == null ) ? parent.locationType : locationType;
		this.world = ( world == null ) ? parent.world : world;
		
		// there will always be a parent, even if it's just 'default'
		isolationPrefix = parent.isolationPrefix;
		// isolation itself is always false, unless set otherwise
		strictMode = parent.strictMode;
		singleRole = parent.singleRole;
		//forgetOnDeath = parent.forgetOnDeath;
		//forgetCooldownTime = parent.forgetCooldownTime;
		
		permitsByUID.putAll( parent.permitsByUID );
	}
	
	
	@Override
	public String toString() {
		String result = "LocationInstance " + locationType + "::" + name + " in "+ world + "\n";
		if ( isolation ) result += "  + isolated\n";
		if ( strictMode ) result += "  + strict\n";
		if ( singleRole ) result += "  + single role\n";
		if ( ! permitsByAlias.isEmpty()) {
			result += "  Permits: ";
			for ( String name : permitsByAlias.keySet()) 
				result += name + " ";
		}
		return result;
	}
	
	
	public void applyRecord( LocationRecord record, PermitMe manager ) {
		// DEBUG
		PermitMe.log.info("[PermitMe] DEBUG: Adding record " + record.name + " to location " + name + "(" + locationType + ") in " + world );
		
		// TODO: Null check and error if location record doesn't contain any settings
		for ( LocationTemplate template : record.settings ) {
			PermitMe.log.info("[PermitMe] DEBUG: Adding settings from " + template.name );
			// TODO: Remove amenisa settings
			/*if ( template.forgetCooldownTime > 0 ) 
				this.forgetCooldownTime = template.forgetCooldownTime;
			if ( template.forgetOnDeath != TFU.UNDEFINED )
				this.forgetOnDeath = template.forgetOnDeath.toBoolean(); */
			if ( template.isolation != TFU.UNDEFINED )
				this.isolation = template.isolation.toBoolean();
			if ( template.singleRole != TFU.UNDEFINED )
				this.singleRole = template.singleRole.toBoolean();
			if ( template.strictMode != TFU.UNDEFINED )
				this.strictMode = template.strictMode.toBoolean();
			if ( template.rawPermitUIDList != null ) {
				for ( String permitUID : template.rawPermitUIDList ) {
					Permit permit = manager.permits.permitsByUUID.get( permitUID );
					// TODO: Error on null
					if ( permit != null ) {
						permitsByUID.put( permit.UUID, permit );
						PermitMe.log.info("[PermitMe] DEBUG: Successfully added permit " + permitUID );
					} else {
						PermitMe.log.warning("[PermitMe] DEBUG: Adding of permit " + permitUID + " failed");
					}
				}
			} else {
				PermitMe.log.warning("[PermitMe] DEBUG: Unable to add permits, as rawPermitUIDList is null" );
			}
		}
	}
	
	
	public void buildIndexData( PermitMe manager ) {
		// if isolated, we need to update the isoloationPrefix and add alias for each permit..
		if ( isolation ) {
			String prefix = "";
			LocationInstance currentInstance = this;
			while ( currentInstance != null ) {
				prefix = currentInstance.name + "." + prefix;
				currentInstance = currentInstance.parent;
			}
			// add type.world to prefix start
			isolationPrefix = locationType + "." + prefix;
		}
		// now that the prefix is known to be correct, we need to build the permit indexes...
		for ( Permit permit : permitsByUID.values()) {
			String alias = ( isolationPrefix.length() > 0 ) ? isolationPrefix + permit.UUID : permit.UUID;
			permitsByAlias.put( alias, permit );
			if ( isolation ) 
				manager.permits.addAlias( permit, alias );
			// and while we're here, add the sign index
			permitsBySignName.put( permit.signName, permit );
			// and add the permit to the reverse indexes
			reverseIndex( alias, permit.blockBreak, blockBreakingIndex );
			reverseIndex( alias, permit.blockBreakComplex, blockBreakingComplexIndex );
			reverseIndex( alias, permit.blockPlace, blockPlacingIndex );
			reverseIndex( alias, permit.blockPlaceComplex, blockPlacingComplexIndex );
			reverseIndex( alias, permit.blockUse, blockUseIndex );
			reverseIndex( alias, permit.blockUseComplex, blockUseComplexIndex );
			reverseIndex( alias, permit.itemUse, itemUseIndex );
			reverseIndex( alias, permit.itemUseComplex, itemUseComplexIndex );
			reverseIndex( alias, permit.crafting, itemCraftingIndex );
			reverseIndex( alias, permit.craftingComplex, itemCraftingComplexIndex );
		}		
	}
	
	
	private void reverseIndex( String name, LinkedList< Integer > source, ReversePermitRecord target ) {
		for ( Integer i : source )
			target.addRecord( i , name );
	}
	
	
	private void reverseIndex( String name, BlockDataRecord source, ReverseComplexPermitRecord target ) {
		for ( Integer id : source.keySet()) {
			LinkedList< Integer > dataValues = source.get( id );
			for ( Integer data : dataValues ) {
				target.addRecord( id, data, name );
			}
		}
	}
	
}
