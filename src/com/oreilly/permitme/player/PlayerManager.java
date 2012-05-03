package com.oreilly.permitme.player;

import java.util.HashMap;
import java.util.Set;

import org.bukkit.entity.Player;

import com.oreilly.permitme.Config;
import com.oreilly.permitme.PermitMe;



public class PlayerManager {

	public HashMap< String, PermitPlayer > players = new HashMap< String, PermitPlayer >();
	
	//private PermitMe manager;

	
	public PlayerManager( PermitMe manager ) {
		//this.manager = manager;
	}
	
	
	public void addPlayer(PermitPlayer player) {
		players.put( player.name, player );
	}


	public void save() {
		for ( PermitPlayer player : players.values())
			Config.savePlayer(player);
	}


	public boolean hasPermit( Player player, Set<String> permitsToCheck) {
		PermitPlayer permitPlayer = players.get( player.getName());
		if ( permitPlayer == null ) {
			PermitMe.log.info("[PermitMe]  Player " + player.getName() + " has no permits" );
			return false;
		}
		for ( String name : permitsToCheck )
			if ( permitPlayer.permits.contains( name )) return true;
		return false;
	}


	public boolean hasPermission(Player player, String string) {
		// TODO Auto-generated method stub
		return false;
	}
	
}
