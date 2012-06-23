package com.oreilly.permitme.record;

import java.util.LinkedList;
import java.util.List;

// TODO: Class


// this represents the contents of a yml / sql based location record

public class LocationRecord {
	
	public String name = null;
	public String locationType = null;
	public String world = null;
	public List< String > settingByName = new LinkedList< String >();
	public List< LocationTemplate > settings = new LinkedList< LocationTemplate >();
	
	
	public LocationRecord ( String name, String type, String world, List< String > settings ) {
		this.name = name;
		this.locationType = type;
		this.world = world;
		this.settingByName = settings; 
	}
	
	
	public String toHumanString() {
		String result = "Location Record (" + locationType + ") in " + world + " named " + name;
		result += "\n  Template strings:  ";
		for ( String name : settingByName )
			result += name + " ";
		result += "\n  Templates by data: ";
		for ( LocationTemplate template : settings )
			result += template.name + " ";
		return result;
	}
}
