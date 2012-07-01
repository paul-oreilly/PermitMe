package com.oreilly.permitme.events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import com.oreilly.permitme.record.Permit;
import com.oreilly.permitme.record.PermitPlayer;

public class PermitMePlayerAddPermitEvent extends Event {

    private static final HandlerList handlers = new HandlerList();
    
    public PermitPlayer permitPlayer = null;
    public Permit permit;
    public String permitAlias;
    public boolean allow;

    
    public PermitMePlayerAddPermitEvent( PermitPlayer permitPlayer, Permit permit,
    		String permitAlias, boolean allow ){
    	this.permitPlayer = permitPlayer;
    	this.permit = permit;
    	this.permitAlias = permitAlias;
    	this.allow = allow;
    }
    
    @Override
	public HandlerList getHandlers() {
        return handlers;
    }
 
    public static HandlerList getHandlerList() {
        return handlers;
    }

}
