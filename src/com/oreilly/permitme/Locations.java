package com.oreilly.permitme;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.World;

import com.oreilly.permitme.data.AllLocationsListNode;
import com.oreilly.permitme.data.AllLocationsResult;
import com.oreilly.permitme.data.LocationInstance;
import com.oreilly.permitme.data.LocationInstanceRecord;
import com.oreilly.permitme.record.LocationRecord;
import com.oreilly.permitme.record.LocationTemplate;
import com.oreilly.permitme.services.LocationResolver;
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
	
	
	// location records - stored by "type name"::"record name" -> LocationRecord
	// eg "world"::"nether", "residence"::"myhome", "worldguard"::"spawn" etc
	HashMap< String, HashMap< String, LocationRecord >> records = 
		new HashMap< String, HashMap< String, LocationRecord >>();
	
	// world -> location data
	public HashMap< String, LocationInstance > worldInstances = new HashMap< String, LocationInstance >();
	// world -> area type -> name -> data
	public LocationInstanceRecord< LocationInstance > locationInstances = new LocationInstanceRecord< LocationInstance >();
	
	// TODO: Add config support for this
	public LocationInstance defaultLocationInstance = null;


	
		
	private PermitMe manager = null;
	
	
	public Locations( PermitMe manager ) {
		this.manager = manager;
		new LocationResolverResidence( "residence" );
		new LocationResolverWorldGuard( "worldguard" );
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
		}
	}
	
	
	public void removeLocationRecord( LocationRecord locationRecord ) {
		// TODO: removeLocationRecord
	}
	
	
	public void loadingComplete() {
		
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
				if ( Settings.debugMode )
					if ( Settings.debugLocationInstanceCreation ) {
						PermitMe.log.info("[PermitMe] DEBUG: Location instance created for world " + worldName + " with record.");
						PermitMe.log.info( record.toHumanString());
					}
			} else
				if ( Settings.debugMode )
					if ( Settings.debugLocationInstanceCreation )
						PermitMe.log.info("[PermitMe] DEBUG: Location instance created for world " + worldName + ". No record." );
			instance.buildIndexData( manager );
			worldInstances.put( worldName, instance );
			// DEBUG:
			if ( Settings.debugMode )
				if ( Settings.debugLocationInstanceCreation )
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


	public void initLocationResolvers() {
		for ( LocationResolver resolver : LocationResolver.disabled )
			resolver.attemptEnable();
	}
	
	// TODO: Rework this later!
	public void locationResolverEnableSucess( LocationResolver source ) {
		PermitMe.log.info("[PermitMe] DEBUG: Location Resolver success for " + source.resolverName );
		// get the list of all areas, and create an instance for each area
		AllLocationsResult locations = source.getAllLocations();
		// start with root nodes
		LinkedList< AllLocationsListNode > rootNodes = getRootNodes( locations );
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
			if ( Settings.debugMode )
				if ( Settings.debugLocationInstanceCreation ) {
					PermitMe.log.info("DEBUG - New root location instance");
					PermitMe.log.info( instance.toString());
				}
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
				// show debug info if required		
				if ( Settings.debugMode )
					if ( Settings.debugLocationInstanceCreation ) {
						PermitMe.log.info("DEBUG - New descendent location instance");
						PermitMe.log.info( instance.toString());			
					}
				// add any children of this child
				AllLocationsListNode node = locations.get( currentName );
				if ( node == null ) continue;
				if ( node.children == null ) continue;
				int childrenSize = node.children.size();
				if ( childrenSize == 0 ) continue;
				allChildren.addAll( node.children );
				for ( int i = 0; i < childrenSize; i++ ) {
					parents.add( currentName );
					worlds.add( node.world );
				}
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
	
	
	public List< LocationInstance > getLocationInstances( Location location ) {
		
		List< LocationInstance > result = new LinkedList< LocationInstance >();
		String worldName = location.getWorld().getName();
		
		// when resolved, we get a list of strings from the most local to the most general
		//   for instance, kitchen->house->west_wing->big_base
		//   we keep searching, and break when we find a matching a permit location (which would have inheritied from the others)
		for ( LocationResolver resolver : LocationResolver.enabled ) {
			if ( resolver == null ) 
				continue; //TODO: Log, error message etc
			List< String > names = resolver.resolveLocation( location );
			if ( names == null ) 
				continue;
			for ( String name : names  ) {
				LocationInstance instance = locationInstances.getRecord( worldName, resolver.resolverName, name );
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
	
}
