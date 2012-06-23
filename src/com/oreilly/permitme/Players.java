package com.oreilly.permitme;

import java.util.HashMap;
import java.util.Set;

import org.bukkit.entity.Player;

import com.oreilly.permitme.record.Permit;
import com.oreilly.permitme.record.PermitPlayer;



public class Players {

	public HashMap< String, PermitPlayer > players = new HashMap< String, PermitPlayer >();
	
	
	public Players() {
	}
	
	
	public void addPlayer(PermitPlayer player) {
		players.put( player.name, player );
	}


	public void save() {
		for ( PermitPlayer player : players.values())
			Config.savePlayer(player);
	}

	
	public boolean hasPermit( Player player, HashMap< String, Permit> permits ) {
		PermitPlayer permitPlayer = players.get( player.getName());
		if ( permitPlayer == null ) {
			PermitMe.log.info("[PermitMe]  Player " + player.getName() + " has no permits" );
			return false;
		}
		for ( Permit permit : permits.values())
			if ( permitPlayer.permits.contains( permit.UUID )) return true;
		return false;
	}
	
	// TODO: Remove
	public boolean hasPermit( Player player, Set<String> permitsAliasList) {
		PermitPlayer permitPlayer = players.get( player.getName());
		if ( permitPlayer == null ) {
			PermitMe.log.info("[PermitMe]  Player " + player.getName() + " has no permits" );
			return false;
		}
		for ( String alias : permitsAliasList )
			if ( permitPlayer.permits.contains( alias )) return true;
		return false;
	}


	public boolean hasPermission(Player player, String string) {
		// TODO Auto-generated method stub - hasPermission
		return false;
	}
	
}
