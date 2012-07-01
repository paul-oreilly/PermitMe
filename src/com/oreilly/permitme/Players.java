package com.oreilly.permitme;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.bukkit.entity.Player;

import com.oreilly.permitme.record.PermitPlayer;


@SuppressWarnings("serial")
class PlayersList extends HashSet< PermitPlayer >{}


public class Players {

	private final HashMap< String, PermitPlayer > players = new HashMap< String, PermitPlayer >();
	private final HashMap< String, PlayersList > playersByPermit = new HashMap< String, PlayersList >();
	
	public Players() {
	}
	
	
	public void addPlayer(PermitPlayer player) {
		players.put( player.name, player );
	}
	
	
	public PermitPlayer getPlayer( String playerName ) {
		PermitPlayer player = players.get( playerName );
		if ( player == null ) {
			player = new PermitPlayer( playerName );
			addPlayer( player );
		}
		return player;
	}
	
	
	public int getPlayerCountByPermit( String permitAlias ) {
		PlayersList list = playersByPermit.get( permitAlias );
		return ( list == null ) ? 0 : list.size();
	}
	
	
	public HashMap< String, HashSet< PermitPlayer >> getAllPlayerCountsByPermit() {
		HashMap< String, HashSet< PermitPlayer >> result = new HashMap< String, HashSet< PermitPlayer >>();
		for ( String key : playersByPermit.keySet()) {
			PlayersList list = playersByPermit.get( key );
			HashSet< PermitPlayer > newList = new HashSet< PermitPlayer >();
			newList.addAll( list );
			result.put( key, newList );
		}
		return result;
	}


	public void save() {
		for ( PermitPlayer player : players.values())
			Config.savePlayer(player);
	}
	
	
	public void addPermit( String permitAlias, PermitPlayer player ) {
		if ( player.permits.contains( permitAlias )) return;
		player.permits.add( permitAlias );
		PlayersList list = playersByPermit.get( permitAlias );
		if ( list == null ) {
			list = new PlayersList();
			playersByPermit.put( permitAlias, list );
		}
		list.add( player );
	}
	
	
	public void removePermit( String permitAlias, PermitPlayer player ) {
		if ( player.permits.contains( permitAlias )) {
			player.permits.remove( permitAlias );
			playersByPermit.get( permitAlias ).remove( player );
		}
	}
	
	
	// TODO: Add support for universal permits (held by all players)
	public boolean hasPermit( Player player, Set<String> permitsAliasList ) {
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
