package com.oreilly.permitme.record;

import java.util.List;

import com.oreilly.permitme.data.TFU;

// TODO: Class

public class LocationTemplate {
	
	// data from config file
	public String name = null;
	public TFU isolation = TFU.UNDEFINED;
	public TFU strictMode = TFU.UNDEFINED;			// Implement later
	public TFU singleRole = TFU.UNDEFINED;			// single role will imply isolation
	//public boolean costModifierDefined = false;
	//public double costModifier = 1;
	//public TFU forgetOnDeath = TFU.UNDEFINED;
	//public long forgetCooldownTime = -1;  // -1 is "undefined"
	public List< String > rawPermitUIDList = null;
	
	
	public LocationTemplate( String name ) {
		this.name = name;
	}
	
	
	public String toHumanString() {
		String result = "Location Template named " + name;
		result += "\n  isolation is " + isolation.toString();
		result += "\n  strict mode is " + strictMode.toString();
		result += "\n  single role is " + singleRole.toString();
		if ( rawPermitUIDList == null )
			result += "\n  No permits are listed.";
		else {
			result += "\n  Listed permits (" + rawPermitUIDList.size() + ") are: ";
			for ( String name : rawPermitUIDList ) 
				result += name + " ";
		}
		return result;
	}
}
