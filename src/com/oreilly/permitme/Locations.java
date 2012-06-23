package com.oreilly.permitme;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Location;
import org.bukkit.World;

import com.oreilly.permitme.api.LocationResolver;
import com.oreilly.permitme.data.AllLocationsListNode;
import com.oreilly.permitme.data.AllLocationsResult;
import com.oreilly.permitme.data.LocationInstance;
import com.oreilly.permitme.data.LocationInstanceRecord;
import com.oreilly.permitme.record.LocationRecord;
import com.oreilly.permitme.record.LocationTemplate;
import com.oreilly.permitme.services.LocationResolverResidence;
import com.oreilly.permitme.services.LocationResolverWorldGuard;

/**
 * 
 * @author Paul O'Reilly
 *	
 *  Permits hold the restriction data
 *  Location Templates hold a list of permits, and settings for a location
 *  Location records are based from one (or more) templates, linked to a location (defined by a location resolver)
 *  Location instances, based from records and full inheritance chains based on locations are generated
 *  There should always be an instance for every defined region of a location resolver, and each world (in general)
 *  Instances are not saved, and they represent the end result of all inheritance etc from the parent areas.
 *  
 *  If a location record has multiple templates applied, they are to be applied in the order specified (in case of clashes)
 *  ( This likely means a "default" template can be defined in config, and always applied -first-, 
 *  allowing other settings to override)
 *  
 *  
 *
 */

public class Locations {

	// location templates hold setting information (eg "block this", "craft that")
	HashMap< String, LocationTemplate > templates = new HashMap< String, LocationTemplate >();
	
	// used to store services that resolve locations into names (eg residence and worldguard)
	HashMap< String, LocationResolver > locationResolvers = new HashMap< String, LocationResolver >();
	LinkedList< String > locationTypes = new LinkedList< String >();
	HashMap< String, LocationResolver > enabledLocationResolvers = new HashMap< String, LocationResolver >();
	
	// location records - stored by "type name"::"record name" -> LocationRecord
	// eg "world"::"nether", "residence"::"myhome", "worldguard"::"spawn" etc
	HashMap< String, HashMap< String, LocationRecord >> records = 
		new HashMap< String, HashMap< String, LocationRecord >>();
	
	// world -> location data
	public HashMap< String, LocationInstance > worldInstances = new HashMap< String, LocationInstance >();
	// world -> area type -> name -> data
	public LocationInstanceRecord< LocationInstance > locationInstances = new LocationInstanceRecord< LocationInstance >();
	
	// TODO: Init this
	public LocationInstance defaultLocationInstance = null;


	
		
	private PermitMe manager = null;
	
	
	public Locations( PermitMe manager ) {
		this.manager = manager;
		addLocationResolver( new LocationResolverResidence( "residence", this ));
		addLocationResolver( new LocationResolverWorldGuard( "worldguard", this ));
	}
	
	
	public void addLocationResolver( LocationResolver resolver ) {
		String name = resolver.resolverName;
		locationTypes.add( name );
		locationResolvers.put( name, resolver );
	}
	
	
	public void addLocationTemplate( LocationTemplate template ) {
		templates.put( template.name, template );
	}
	
	
	public void addLocationRecords( List< LocationRecord > locationRecords ) {
		for ( LocationRecord locationRecord : locationRecords ) {
			if ( locationRecord == null ) continue;
			// match settingsByName to location template records
			for ( String settingName : locationRecord.settingByName ) {
				LocationTemplate template = templates.get( settingName );
				if ( template == null ) {
					PermitMe.log.warning("[PermitMe] Location record " + locationRecord.toString() + 
							" references the template " + settingName + " which doesn't exist" );
					continue;
				}
				locationRecord.settings.add( template );
			}
			// add the record
			HashMap< String, LocationRecord > outer = records.get( locationRecord.locationType );
			if ( outer == null ) {
				outer = new HashMap< String, LocationRecord >();
				records.put( locationRecord.locationType, outer );
			}
			// TODO: Collision check, reporting
			outer.put( locationRecord.name, locationRecord );
			PermitMe.log.info("[PermitMe] DEBUG: Location record added:\n" + locationRecord.toHumanString());
		}
	}
	
	
	public void removeLocationRecord( LocationRecord locationRecord ) {
		// TODO: removeLocationRecord
	}
	
	
	public void loadingComplete() {
		// TODO: Method loadingComplete
		// TODO: Check we have a default location record
		
		if ( defaultLocationInstance == null )
			defaultLocationInstance = new LocationInstance( "default" );
		
		if ( records.get("world") == null )
			records.put( "world", new HashMap< String, LocationRecord >());
		
		// Create instances for each existing world
		for ( World world : PermitMe.instance.server.getWorlds()) {
			String worldName = world.getName();
			LocationRecord record = records.get("world").get( worldName );
			LocationInstance instance = new LocationInstance( worldName, defaultLocationInstance, "world", worldName );
			if ( record != null ) {
				instance.applyRecord( record, manager );
				// DEBUG:
				PermitMe.log.info("[PermitMe] DEBUG: Location instance created for world " + worldName + " with record.");
				PermitMe.log.info( record.toHumanString());
			} else
				PermitMe.log.info("[PermitMe] DEBUG: Location instance created for world " + worldName + ". No record." );
			instance.buildIndexData( manager );
			worldInstances.put( worldName, instance );
			// DEBUG:
			PermitMe.log.info("[PermitMe] DEBUG information for world instance:\n" + instance );
		}
		
		// set a task to attempt enabling of locationResolvers
		PermitMe.instance.server.getScheduler().scheduleSyncRepeatingTask( PermitMe.instance, 
				new	Runnable() { 
					@Override
					public void run() {
						initLocationResolvers();
					} 
				}, 20, 20 );
	}


	// TODO: Set up a timer to call this every x until all resolvers are initialised
	public void initLocationResolvers() {
		// TODO: Method initLocationResolvers
		for ( LocationResolver resolver : locationResolvers.values()) {
			if ( enabledLocationResolvers.containsValue( resolver )) continue;
			resolver.attemptEnable();
		}
	}
	
	
	public void locationResolverEnableSucess( LocationResolver source ) {
		// TODO:
		// get the list of all areas, and create an instance for each area
		enabledLocationResolvers.put( source.resolverName, source );
		AllLocationsResult locations = source.getAllLocations();
		// debug information to console to show we have loaded successfullly
		PermitMe.log.info("DEBUG: Location data starting for " + source.resolverName );
		LinkedList< AllLocationsListNode > rootNodes = getRootNodes( locations );
		for ( AllLocationsListNode node : rootNodes ) {
			if ( node == null ) continue;
			PermitMe.log.info( node.name );
			if ( node.children == null ) continue;
			if ( node.children.size() == 0 ) continue;
			LinkedList< String > children = new LinkedList< String >();
			LinkedList< Integer > indent = new LinkedList< Integer >();
			for ( String name : node.children ) {
				children.add( name );
				indent.add( 2 );
			}
			while ( children.size() > 0 ) {
				Integer currentIndent = indent.removeLast();
				String currentName = children.removeLast();
				PermitMe.log.info( StringUtils.repeat( " ", currentIndent ) + currentName );
				AllLocationsListNode currentNode = locations.get( currentName );
				if ( currentNode == null ) continue;
				if ( currentNode.children == null ) continue;
				for ( String name : currentNode.children ) {
					children.add( name );
					indent.add( currentIndent + 2 );
				}
			}
		}
		PermitMe.log.info("DEBUG: Location data for " + source.resolverName + " complete." );
		
		// TODO: real work - make a location instance to match each node
		
		// start with root nodes
		for ( AllLocationsListNode rootNode : rootNodes ) { 
			// make instance for the root mode, inheriting from the world
			LocationInstance worldParent = worldInstances.get( rootNode.world );
			LocationInstance instance = new LocationInstance( rootNode.name, worldParent );
			instance.locationType = source.resolverName;
			HashMap< String, LocationRecord > recordSet = records.get( source.resolverName );
			LocationRecord record = ( recordSet == null ) ? null : recordSet.get( rootNode.name );
			if (record != null )
				instance.applyRecord( record, manager );
			instance.buildIndexData( manager );
			locationInstances.addRecord( rootNode.world, source.resolverName, rootNode.name, instance );
			// DEBUG
			PermitMe.log.info("DEBUG - New root location instance");
			PermitMe.log.info( instance.toString());
			// now that the root node has an instance, do the same for the children..
			if ( rootNode.children == null ) continue;
			if ( rootNode.children.size() == 0 ) continue;
			LinkedList< String > allChildren = new LinkedList< String >();
			allChildren.addAll( rootNode.children );
			LinkedList< String > parents = new LinkedList< String >();
			LinkedList< String > worlds = new LinkedList< String >();
			for ( int i = 0; i < allChildren.size(); i++ ) {
				parents.add( rootNode.name );
				worlds.add( rootNode.world );
			}
			while ( allChildren.size() > 0 ) {
				String currentName = allChildren.removeLast();
				String parentName = parents.removeLast();
				String parentWorldName = worlds.removeLast();
				LocationInstance parent = locationInstances.getRecord( parentWorldName, source.resolverName, parentName );
				instance = new LocationInstance( currentName, parent );
				recordSet = records.get( source.resolverName );
				record = ( recordSet == null ) ? null : recordSet.get( currentName );
				if ( record != null )
					instance.applyRecord( record, manager );
				instance.buildIndexData( manager );
				locationInstances.addRecord( instance.world, source.resolverName, instance.name, instance );
				// DEBUG
				PermitMe.log.info("DEBUG - New descendent location instance");
				PermitMe.log.info( instance.toString());				
			}
		}
	}
	
	
	private LinkedList< AllLocationsListNode > getRootNodes( AllLocationsResult source ) {
		LinkedList< AllLocationsListNode > result = new LinkedList< AllLocationsListNode >();
		for ( AllLocationsListNode node : source.values()) {
			if ( node.parent == null )
				result.add( node );
		}
		return result;
	}
	
	/*
	public void addPermitLocations( List< OldPermitLocation > locations ) {
		for ( OldPermitLocation location : locations ) {
			if ( location.typeDetails.overallType == LocationTypeEnum.UNIVERSAL ) {
				// no inheritance to resolve, so add directly to universalSettings
				universalSettings.put( location.locationName, location );
				continue;
			}
			if ( location.typeDetails.overallType == LocationTypeEnum.WORLD )
				// also no inheritance...
				worldData.put( location.locationName, location );
			else
				// anything else waits until we have loaded all the information, and can therefore resolve inheritance
				queuedAreas.add( location );
		}
	}*/
	
/*	
	public void configComplete() {
		
		LinkedList< OldPermitLocation > toInit = new LinkedList< OldPermitLocation >();
		toInit.addAll( universalSettings.values());
		toInit.addAll( worldData.values());
		toInit.addAll( queuedAreas );
		
		LinkedList< OldPermitLocation > unresolved = new LinkedList< OldPermitLocation >();
		boolean madeADifference;
		boolean failure = false;
		OldPermitLocation target = null;
		LinkedList< OldPermitLocation > inheritance = null;
		
		while ( toInit.size() > 0 ) {
			madeADifference = false;
			for ( OldPermitLocation location : toInit ) {
				// see if we can resolve all this permits inheritance
				failure = false; target = null;
				inheritance = new LinkedList< OldPermitLocation >();
				for ( LocationInheritance li : location.inheritance ) {
					switch ( li.typeData.overallType) {
						case UNIVERSAL: target = universalSettings.get( li.name ); break;
						case WORLD: target = worldData.get( li.name ); break;
						case AREA: target = areaData.getRecord( li.worldName, li.typeData.detail, li.name );
					}
					if ( target == null ) {
						failure = true; break; }
					if ( target.initialised == false ) {
						failure = true; break; }
					inheritance.add( target );
				}
				// if we can't resolve at this point, then move to the next item
				if ( failure ) continue;
				// we can resolve all, so assign inherited data
				// (all reverse indexes, add all permit objects)
				for ( OldPermitLocation inherit : inheritance ) {
					location.blockBreakingComplexIndex.addAll( inherit.blockBreakingComplexIndex );
					location.blockBreakingIndex.addAll( inherit.blockBreakingIndex );
					location.blockPlacingComplexIndex.addAll( inherit.blockPlacingComplexIndex );
					location.blockPlacingIndex.addAll( inherit.blockPlacingIndex );
					location.blockUseComplexIndex.addAll( inherit.blockUseComplexIndex );
					location.blockUseIndex.addAll( inherit.blockUseIndex );
					location.itemCraftingComplexIndex.addAll( inherit.itemCraftingComplexIndex );
					location.itemCraftingIndex.addAll( inherit.itemCraftingIndex );
					location.itemUseComplexIndex.addAll( inherit.itemUseComplexIndex );
					location.itemUseIndex.addAll( inherit.itemUseIndex );
					location.permitsBySignName.putAll( inherit.permitsBySignName );
					location.permitsByUUID.putAll( inherit.permitsByUUID );
				}
				
				// resolve the permits for this object (overwriting any clashes from inherited data)
				for ( String permitUUID : location.rawPermitList ) {
					Permit permit = manager.permits.permitsByUUID.get( permitUUID );
					// if there's no permit, note the error and continue
					if ( permit == null ) {
						// TODO: Error msg
						continue;
					}
					else {
						location.permitsByUUID.put( permitUUID, permit );
						location.permitsBySignName.put( permit.signName, permit );
						// build a reverse index for event checking (appending to inherited data) for each added permit
						reverseIndex( location, permit );
					}	
				}
				// set initialised to true, and note progress
				location.initialised = true;
				madeADifference = true;
			}
			// if we havn't made a difference, then the remain permits are unable to be resolved - so give an error and break
			if ( ! madeADifference ) {
				// TODO: Error message, list of permits
				break;
			} else
				// all the unresolved permits become the new toInit
				toInit = unresolved;	
		}	
	}
	*/
	
	public List< LocationInstance > getLocationInstances( Location location ) {
		
		List< LocationInstance > result = new LinkedList< LocationInstance >();
		String worldName = location.getWorld().getName();
		
		// when resolved, we get a list of strings from the most local to the most general
		//   for instance, kitchen->house->west_wing->big_base
		//   we keep searching, and break when we find a matching a permit location (which would have inheritied from the others)
		for ( String locationType : locationTypes ) {
			LocationResolver resolver = locationResolvers.get( locationType );
			if ( resolver == null ) 
				continue; //TODO: Log, error message etc
			List< String > names = resolver.resolveLocation( location );
			if ( names == null ) 
				continue;
			for ( String name : names  ) {
				LocationInstance instance = locationInstances.getRecord( worldName, locationType, name );
				if ( instance != null )
					result.add( instance );
			}
		}
		
		// only if there are no area's, do we add the world record
		if ( result.size() == 0 ) {
			LocationInstance wd = worldInstances.get( worldName );
			if ( wd != null )
				result.add( wd );
		}
		
		return result;
	}
	
	
/*
	private void reverseIndex( OldPermitLocation location, Permit permit ) {
		String name = permit.UUID;
		for ( Integer id : permit.blockBreak ) 
			location.blockBreakingIndex.addRecord( id, name );
		for ( Integer id : permit.blockPlace ) 
			location.blockPlacingIndex.addRecord( id, name );
		for ( Integer id : permit.blockUse ) 
			location.blockUseIndex.addRecord( id, name );
		for ( Integer id : permit.itemUse ) 
			location.itemUseIndex.addRecord( id, name );
		for ( Integer id : permit.crafting ) 
			location.itemCraftingIndex.addRecord( id, name );
		reverseIndex( name, location.blockBreakingComplexIndex, permit.blockBreakComplex );
		reverseIndex( name, location.blockPlacingComplexIndex, permit.blockPlaceComplex );
		reverseIndex( name, location.blockUseComplexIndex, permit.blockUseComplex );
		reverseIndex( name, location.itemUseComplexIndex, permit.itemUseComplex );
		reverseIndex( name, location.itemCraftingComplexIndex, permit.craftingComplex );
	}

	
	
	private void reverseIndex( String name, ReverseComplexPermitRecord target, BlockDataRecord source ) {
		for ( Integer id : source.keySet()) {
			List< Integer > record = source.get( id );
			if ( record != null )
				for ( Integer data : record )
					target.addRecord( id, data, name );		
		}
	}	*/
}
