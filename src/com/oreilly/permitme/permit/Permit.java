package com.oreilly.permitme.permit;

import java.util.LinkedList;
import java.util.List;



/*
 * A simple data structure is used to improve speed
 * (allowing all values to read directly)
 * Any complex logic is handled in the permit manager class
 * 
 * Any meta data tables are MetadataRecod's
 */



public class Permit {

	public String name;
	public String filename;
	public double basePrice;
	public boolean virtual;
	public String pricingMethod;
	public List< PermitRatio > ratios = new LinkedList< PermitRatio >();
	public LinkedList<Integer> blockBreak = new LinkedList<Integer>();
	public BlockDataRecord blockBreakMeta = new BlockDataRecord();  
	public LinkedList<Integer> blockPlace = new LinkedList<Integer>();
	public BlockDataRecord blockPlaceMeta = new BlockDataRecord();
	public LinkedList<Integer> itemUse = new LinkedList<Integer>();
	public BlockDataRecord itemUseMeta = new BlockDataRecord();
	public LinkedList<Integer> crafting = new LinkedList<Integer>();
	public BlockDataRecord craftingMeta = new BlockDataRecord();
	public PermitPermission enchanting;
	public PermitPermission golem;
	
	public Permit( String name, String filename ) {
		this.name = name;
		this.filename = filename;
	}

}

