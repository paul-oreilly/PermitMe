package com.oreilly.permitme.api;

import java.util.LinkedList;

import org.bukkit.Location;
import org.bukkit.plugin.Plugin;

import com.oreilly.permitme.Locations;
import com.oreilly.permitme.data.AllLocationsResult;



public abstract class LocationResolver {

	public String resolverName = null;
	public boolean initialised = false;
	
	public Locations manager;
	
	
	public LocationResolver( String name, Locations manager ) {
		this.resolverName = name;
		this.manager = manager;
		manager.addLocationResolver( this );
	}

	// gives back a list of locations that match 
	// residence would only give one (eg "kitchen"), where-as worldguard allows overlapping areas,
	//  so needs to be able to return a list (eg the corner where "mall" might overlap with "spawn")
	abstract public LinkedList< String > resolveLocation( Location location );

	
	abstract public AllLocationsResult getAllLocations();

	
	// this function will be called every x seconds until it returns true
	public boolean attemptEnable() {
		if ( enable()) {
			initialised = true;
			manager.locationResolverEnableSucess( this );
			return true;
		}
		return false;
	}
	
	// method to be overriden to connect to provider, do setup etc
	//  returns true on success
	//  will be called again later if returns false
	abstract protected boolean enable();
	
	
	public void notifyPluginDependency( Plugin plugin ) {
		// TODO: notifyPluginDependency
		// lets Locations know that if 'plugin' is disabled, then this resolver is disabled also.
		//  locations needs to listen to pluginDisabled events, then deal with it's state issues.
	}
	
	
	// called if a dependent plugin is disabled, and also when PermitMe is due to be disabled
	public void disable() {
		this.initialised = false;
	}
}
