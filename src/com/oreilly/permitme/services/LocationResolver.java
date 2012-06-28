package com.oreilly.permitme.services;

import java.util.LinkedList;
import java.util.concurrent.ConcurrentSkipListSet;

import org.bukkit.Location;

import com.oreilly.permitme.PermitMe;
import com.oreilly.permitme.data.AllLocationsResult;




public abstract class LocationResolver implements Comparable< LocationResolver > {

	// static data types must be thread safe
	public static ConcurrentSkipListSet< LocationResolver > disabled = 
			new ConcurrentSkipListSet< LocationResolver >();
	public static ConcurrentSkipListSet< LocationResolver > enabled = 
			new ConcurrentSkipListSet< LocationResolver >();
	public static ConcurrentSkipListSet< LocationResolver > all = 
			new ConcurrentSkipListSet< LocationResolver >();
	
	private static int idCount = 1;
	
	public String resolverName = null;
	public boolean initialised = false;
	private int id = 0;
	
	//public Locations manager;
	
	
	public LocationResolver( String name ) {
		this.resolverName = name;
		id = idCount;
		idCount++;
		// TODO: Uniqueness check
		all.add( this );
		attemptEnable();
	}
	
	
	@Override
	public String toString() {
		return resolverName;
	}
	
	
	@Override
	public int compareTo( LocationResolver other ) {
		return ( id - other.id );
	}

	
	// gives back a list of locations that match 
	// residence would only give one (eg "kitchen"), where-as worldguard allows overlapping areas,
	//  so needs to be able to return a list (eg the corner where "mall" might overlap with "spawn")
	abstract public LinkedList< String > resolveLocation( Location location );

	
	abstract public AllLocationsResult getAllLocations();

	
	// this function will be called every x seconds until it returns true
	public boolean attemptEnable() {
		if ( PermitMe.instance != null )
			if ( enable()) {
				initialised = true;
				disabled.remove( this );
				enabled.add( this );
				PermitMe.instance.locations.locationResolverEnableSucess( this );
				return true;
			}
		// failure
		disabled.add( this );
		enabled.remove( this );
		return false;
	}
	
	// method to be overriden to connect to provider, do setup etc
	//  returns true on success
	//  will be called again later if returns false
	abstract protected boolean enable();
	
	
	// called if a dependent plugin is disabled, and also when PermitMe is due to be disabled
	public void disable() {
		this.initialised = false;
		disabled.add( this );
		enabled.add( this );
	}
}
