package com.oreilly.permitme.data;

import java.util.List;


public class AllLocationsListNode {
	
	public String name = null;
	public String parent = null;
	public List< String > children = null;
	public String world = null;
	
	
	public AllLocationsListNode( String name, String parent, List< String > children, String world ) {
		this.name = name;
		this.parent = parent;
		this.children = children;
		this.world = world;
	}

}

