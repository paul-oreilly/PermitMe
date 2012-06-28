package com.oreilly.permitme.events;

import java.util.HashSet;
import java.util.List;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.block.BlockPlaceEvent;

import com.oreilly.permitme.data.LocationInstance;

public class PermitMeBlockPlaceEvent extends Event {

    private static final HandlerList handlers = new HandlerList();
    
    public BlockPlaceEvent orignalEvent = null;
    
    public boolean allowAction = false;
    public List< LocationInstance > locationData = null;
    public HashSet< String > requiredPermits = null;
    public Player player = null;
    
    
    public PermitMeBlockPlaceEvent( BlockPlaceEvent orignalEvent, Player player, 
    		List< LocationInstance > locationInstances, HashSet< String > requiredPermits, 
    		boolean allow ) {
    	this.orignalEvent = orignalEvent;
    	this.player = player;
    	this.locationData = locationInstances;
    	this.requiredPermits = requiredPermits;
    	this.allowAction = allow;
    }
    
    @Override
	public HandlerList getHandlers() {
        return handlers;
    }
 
    public static HandlerList getHandlerList() {
        return handlers;
    }
}
