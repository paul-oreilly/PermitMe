package com.oreilly.permitme.record;

import java.util.LinkedList;
import java.util.List;

import com.oreilly.permitme.data.BlockDataRecord;
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

	public String signName;
	public String UUID;
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
	public BlockDataRecord blockBreakComplex = new BlockDataRecord();  
	public LinkedList<Integer> blockPlace = new LinkedList<Integer>();
	public BlockDataRecord blockPlaceComplex = new BlockDataRecord();
	public LinkedList<Integer> blockUse = new LinkedList<Integer>();
	public BlockDataRecord blockUseComplex = new BlockDataRecord();	
	public LinkedList<Integer> itemUse = new LinkedList<Integer>();
	public BlockDataRecord itemUseComplex = new BlockDataRecord();
	public LinkedList<Integer> crafting = new LinkedList<Integer>();
	public BlockDataRecord craftingComplex = new BlockDataRecord(); 
	
	//public PermitPermission golem;
	// TODO: Add possible item grants to go with permit then extend to autoequip slots
	
	
	public Permit( String UUID, String signName ) {
		this.UUID = UUID;
		this.signName = signName;
	}
	
	
	@Override
	public String toString() {
		return UUID;
	}
	
	
	public String toHumanString() {
		String result = "Permit named " + signName + " with UUID " + UUID;
		String sbreaking = debugString( blockBreak ) + debugString( blockBreakComplex );
		String splacing = debugString( blockPlace ) + debugString( blockPlaceComplex );
		String sblockuse = debugString( blockUse ) + debugString( blockUseComplex );
		String sitemUse = debugString( itemUse ) + debugString( itemUseComplex );
		String scrafting = debugString( crafting ) + debugString( craftingComplex );
		if ( sbreaking.length() > 0 ) result += "\n  breaking: " + sbreaking;
		if ( splacing.length() > 0 ) result += "\n  placing: " + splacing;
		if ( sblockuse.length() > 0 ) result += "\n  block use: " + sblockuse;
		if ( sitemUse.length() > 0 ) result += "\n  item use: " + sitemUse;
		if ( scrafting.length() > 0 ) result += "\n  crafting: " + scrafting;
		return result;
	}
	
	
	private String debugString( LinkedList< Integer > data ) {
		String result = "";
		for ( Integer i : data )
			result += i.toString() + " ";
		return result;
	}
	
	
	private String debugString( BlockDataRecord data ) {
		String result = "";
		for ( Integer id : data.keySet()) {
			LinkedList< Integer > dataSet = data.get( id );
			for ( Integer d : dataSet ) {
				result += id + ":" + d + " ";
			}
		}
		return result;
	}

}

