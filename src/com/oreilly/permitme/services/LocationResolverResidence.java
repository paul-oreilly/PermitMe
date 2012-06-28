package com.oreilly.permitme.services;

import java.util.LinkedList;

import org.bukkit.Location;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import com.bekvon.bukkit.residence.protection.ResidenceManager;
import com.oreilly.permitme.PermitMe;
import com.oreilly.permitme.data.AllLocationsResult;

public class LocationResolverResidence extends LocationResolver {

	
	public LocationResolverResidence( String name ) {
		super( name );
	}


	@Override
	public LinkedList< String > resolveLocation(Location location) {
		LinkedList< String > result = new LinkedList< String >();
		ClaimedResidence res = Residence.getResidenceManager().getByLoc( location );
		if ( res != null ) {
			PermitMe.log.info("[PermitMe] ** Residence test - " + res.getName());
			result.add( res.getName());
			return result;
		}
		return result;
	}
	
	
	@Override
	public AllLocationsResult getAllLocations() {
		// returns a list of all locations
		AllLocationsResult result = new AllLocationsResult();
		ResidenceManager residenceManager = Residence.getResidenceManager();
		// TODO: Error message if null
		if ( residenceManager != null ) {
			for ( String residenceName : residenceManager.getResidenceList()) {
				ClaimedResidence res = residenceManager.getByName( residenceName );
				ClaimedResidence parent = res.getParent();
				String parentName = ( parent == null ) ? null : parent.getName();
				String[] children = res.getSubzoneList();
				String worldName = res.getWorld();
				result.addRecord( worldName, residenceName, parentName, children );
			}
		}
		return result;
	}
	
	
	@Override
	protected boolean enable() {
		return ( Residence.getResidenceManager() != null );
	}
	
}
