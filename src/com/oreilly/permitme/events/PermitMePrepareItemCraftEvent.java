package com.oreilly.permitme.events;

import java.util.HashSet;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.inventory.PrepareItemCraftEvent;

import com.oreilly.permitme.data.LocationInstance;

public class PermitMePrepareItemCraftEvent extends Event {

    private static final HandlerList handlers = new HandlerList();
    
    public PrepareItemCraftEvent orignalEvent = null;
    
    public boolean allowAction = false;
    public List< LocationInstance > locationData = null;
    public HashSet< String > requiredPermits = null;
    public List< Player > players = null;
    public Location location = null;
    
    
    public PermitMePrepareItemCraftEvent( PrepareItemCraftEvent orignalEvent, List< Player > players,
    		List< LocationInstance > locationData, HashSet< String > requiredPermits,
    		boolean allow, Location location ) {
    	this.orignalEvent = orignalEvent;
    	this.players = players;
    	this.locationData = locationData;
    	this.requiredPermits = requiredPermits;
    	this.allowAction = allow;
    	this.location = location;
    }
    
    
    @Override
	public HandlerList getHandlers() {
        return handlers;
    }
 
    public static HandlerList getHandlerList() {
        return handlers;
    }
}
