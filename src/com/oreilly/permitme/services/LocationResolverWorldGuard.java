package com.oreilly.permitme.services;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;

import com.oreilly.permitme.Locations;
import com.oreilly.permitme.PermitMe;
import com.oreilly.permitme.api.LocationResolver;
import com.oreilly.permitme.data.AllLocationsListNode;
import com.oreilly.permitme.data.AllLocationsResult;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;



public class LocationResolverWorldGuard extends LocationResolver {

	public WorldGuardPlugin plugin = null;
	
	
	public LocationResolverWorldGuard( String name, Locations manager ) {
		super( name, manager );
	}
	
	
	@Override
	protected boolean enable() {
		plugin = findplugin();
		return (( plugin != null ) & ( plugin.isEnabled()));
	}

	@Override
	public LinkedList< String > resolveLocation( Location location ) {
		// sanity check - do we have a plugin reference?
		if ( plugin == null ) {
			plugin = findplugin();
			if ( plugin == null ) {
				PermitMe.log.warning("[PermitMe] !! Worldguard plugin not found");
				return null;
			}
		}
		// get the list of regions that contain the given location
		RegionManager regionManager = plugin.getRegionManager( location.getWorld());
		ApplicableRegionSet set = regionManager.getApplicableRegions( location );
		LinkedList< String > parentNames = new LinkedList< String >();
		LinkedList< String > regions = new LinkedList< String >();
		for ( ProtectedRegion region : set ) {
			String id = region.getId();
			regions.add( id );
			ProtectedRegion parent = region.getParent();
			while ( parent != null ) {
				parentNames.add( parent.getId());
				parent = parent.getParent();
			}
		}
		// before we return, we remove any area's that are 'parent' to an existing area
		for ( String name : parentNames )
			regions.remove( name );
		// return the remainder 
		return regions;
	}
	
	private WorldGuardPlugin findplugin() {
		Plugin plugin = PermitMe.instance.server.getPluginManager().getPlugin("WorldGuard");
		if ( plugin == null || !( plugin instanceof WorldGuardPlugin )) {
			PermitMe.log.warning("[PermitMe] WorldGuard plugin not found");
			return null;
		} else
			return (WorldGuardPlugin)plugin;		
	}


	@Override
	public AllLocationsResult getAllLocations() {
		// sanity check - do we have a plugin reference?
		if ( plugin == null ) {
			plugin = findplugin();
			if ( plugin == null ) {
				PermitMe.log.warning("[PermitMe] !! Worldguard plugin not found");
				return null;
			}
		}
		// get the list of regions that contain the given location
		AllLocationsResult result = new AllLocationsResult();
		List< World > worlds = PermitMe.instance.server.getWorlds();
		for ( World world : worlds ) {
			String worldName = world.getName();
			RegionManager regionManager = plugin.getRegionManager( world );
			Map< String, ProtectedRegion > worldRegions = regionManager.getRegions();
			for ( ProtectedRegion region : worldRegions.values()) {
				ProtectedRegion parent = region.getParent();
				String parentName = ( parent == null ) ? null : parent.getId();
				result.addRecord( worldName, region.getId(), parentName );
			}
		}
		// worldguard doesn't provide a way to get children of a region, so we build that index now..
		for ( AllLocationsListNode node : result.values()) {
			AllLocationsListNode parentNode = result.get( node.parent );
			if ( parentNode != null ) {
				if ( parentNode.children == null )
					parentNode.children = new LinkedList< String >();
				parentNode.children.add( node.name );
			}
		}

		return result;
	}
	
}
