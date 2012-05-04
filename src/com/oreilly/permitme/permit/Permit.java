package com.oreilly.permitme.permit;

import java.util.LinkedList;
import java.util.List;

import com.oreilly.permitme.data.BlockDataRecord;
import com.oreilly.permitme.data.PermitPermission;
import com.oreilly.permitme.data.PermitPriceDecayMethod;
import com.oreilly.permitme.data.PermitPricingMethod;
import com.oreilly.permitme.data.PermitRatio;



/*
 * A simple data structure is used to improve speed
 * (allowing all values to read directly)
 * Any complex logic is handled in the permit manager class
 * 
 */




public class Permit {

	public String name;
	public String filename;
	public double basePrice;
	public boolean virtual;
	public List< String > inheritenceAsStrings = new LinkedList< String >(); 
	public List< String > inheritencePending = new LinkedList< String >();
	public List< Permit > inherits = new LinkedList< Permit >();
	public PermitPricingMethod pricingMethod;
	public List< PermitRatio > pricingRatios = new LinkedList< PermitRatio >();
	public double pricingFactorCurrentPrice;
	public double pricingFactorOnPurchase;
	public double pricingFactorOnDecay;
	public PermitPriceDecayMethod pricingDecayMethod;
	public long pricingDecayTime;
	public LinkedList<Integer> blockBreak = new LinkedList<Integer>();
	public BlockDataRecord blockBreakMeta = new BlockDataRecord();  
	public LinkedList<Integer> blockPlace = new LinkedList<Integer>();
	public BlockDataRecord blockPlaceMeta = new BlockDataRecord();
	public LinkedList<Integer> blockUse = new LinkedList<Integer>();
	public BlockDataRecord blockUseMeta = new BlockDataRecord();	
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
	
	@Override
	public String toString() {
		return name;
	}

}

