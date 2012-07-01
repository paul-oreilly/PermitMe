package com.oreilly.permitme;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.logging.Logger;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;

import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import com.oreilly.permitme.data.LocationInstance;
import com.oreilly.permitme.data.PermitAction;
import com.oreilly.permitme.events.PermitMeEnableCompleteEvent;
import com.oreilly.permitme.events.PermitMePlayerAddPermitEvent;
import com.oreilly.permitme.events.PermitMePlayerRemovePermitEvent;
import com.oreilly.permitme.record.Permit;
import com.oreilly.permitme.record.PermitPlayer;


// TODO: Add a string translation class and config file, with language support?


public class PermitMe extends JavaPlugin {

	public static PermitMe instance = null;
	
	public static Logger log = Logger.getLogger("minecraft");
	public static Economy economy = null;
	public static Permission permissions = null;
	
	public Players players;
	public Permits permits;
	public Locations locations;
	
	public Server server;
	
	
	public PermitMe() {
		super();
		Commands.loadCommands();
		instance = this;
	}
	
	@Override
	public void onEnable() {
		server = this.getServer();
		
		// start the managers
		players = new Players();
		permits = new Permits();
		locations = new Locations( this );
		
		// load the config file, permits and player info
		Config.load( this );
		
		registerListeners();
		//loadEconomy();
		loadPermissions();
		
		PermitMeEnableCompleteEvent event = new PermitMeEnableCompleteEvent();
		getServer().getPluginManager().callEvent( event );
	}
	
	
	@Override
	public void onDisable() {
		log.info("[PermitMe] Saving config");
		Config.saveConfig();
		log.info("[PermitMe] Saving players");
		players.save();
		log.info("[PermitMe] Saving permits");
		permits.save();
		log.info("[PermitMe] .. save complete");
		log.info("[PermitMe] -> Disabled.");
		// TODO: Shut down data structures
		instance = null;
	}
	
	
	public boolean addPermitToPlayer( String playerName, String permitAlias ) {
		PermitPlayer permitPlayer = players.getPlayer( playerName );
		Permit permit = permits.permitsByAlias.get( permitAlias );
		if ( permit == null ) {
			PermitMe.log.warning("[PermitMe] addPermitToPlayer has failed" + 
					", as no permit with the alias " + permitAlias + " exists" );
			return false;
		}
		PermitMePlayerAddPermitEvent event = new PermitMePlayerAddPermitEvent( permitPlayer, permit, permitAlias, true );
		getServer().getPluginManager().callEvent( event );
		return event.allow;
	}
	
	
	public boolean removePermitFromPlayer( String playerName, String permitAlias ) {
		PermitPlayer permitPlayer = players.getPlayer( playerName );
		Permit permit = permits.permitsByAlias.get( permitAlias );
		if ( permit == null ) {
			PermitMe.log.warning("[PermitMe] removePermitFromPlayer has failed" + 
					", as no permit with the alias " + permitAlias + " exists" );
			return false;
		}
		PermitMePlayerRemovePermitEvent event = new PermitMePlayerRemovePermitEvent( permitPlayer, permit, permitAlias, true );
		getServer().getPluginManager().callEvent( event );
		return event.allow;
	}
	
	
	
	
	public HashSet< String > getRequiredPermits( PermitAction action, List< LocationInstance > locationInstances,
			int id, int data ) {
		HashSet< String > requiredPermits = new HashSet< String >();
		Collection< String > collection = null;
		switch ( action ) {
			case BLOCKDESTORY: {
				for ( LocationInstance instance : locationInstances ) {
					PermitMe.log.info( instance.toString());
					collection = instance.blockBreakingIndex.get( id );
					if ( collection != null ) requiredPermits.addAll( collection );
					collection = instance.blockBreakingComplexIndex.getRecords( id, data );
					if ( collection != null ) requiredPermits.addAll( collection );
				}
				break;
			}
			case BLOCKPLACE: {
				for ( LocationInstance instance : locationInstances ) {
					PermitMe.log.info( instance.toString());
					collection = instance.blockPlacingIndex.get( id );
					if ( collection != null ) requiredPermits.addAll( collection );
					collection = instance.blockPlacingComplexIndex.getRecords( id, data );
					if ( collection != null ) requiredPermits.addAll( collection );
				}
				break;				
			}
			case BLOCKUSE: {
				for ( LocationInstance instance : locationInstances ) {
					PermitMe.log.info( instance.toString());
					collection = instance.blockUseIndex.get( id );
					if ( collection != null ) requiredPermits.addAll( collection );
					collection = instance.blockUseComplexIndex.getRecords( id, data );
					if ( collection != null ) requiredPermits.addAll( collection );
				}
				break;					
			}
			case ITEMCRAFT: {
				for ( LocationInstance instance : locationInstances ) {
					PermitMe.log.info( instance.toString());
					collection = instance.itemCraftingIndex.get( id );
					if ( collection != null ) requiredPermits.addAll( collection );
					collection = instance.itemCraftingComplexIndex.getRecords( id, data );
					if ( collection != null ) requiredPermits.addAll( collection );
				}
				break;				
			}
			case ITEMUSE: {
				for ( LocationInstance instance : locationInstances ) {
					PermitMe.log.info( instance.toString());
					collection = instance.itemUseIndex.get( id );
					if ( collection != null ) requiredPermits.addAll( collection );
					collection = instance.itemUseComplexIndex.getRecords( id, data );
					if ( collection != null ) requiredPermits.addAll( collection );
				}
				break;					
			}
		}		
		return requiredPermits;
	}
	
	
	public boolean isActionAllowed( Player player, PermitAction action, Location location, int id, int data ) {
		if ( players.hasPermission( player, "exempt" )) return true;
		List< LocationInstance > locationInstances = locations.getLocationInstances( location );
		HashSet< String > requiredPermits = getRequiredPermits( action, locationInstances, id, data );
		return isActionAllowed( player, action, location, locationInstances, requiredPermits, id, data );
	}
	
	
	// TODO: Location settings - strict handling
	public boolean isActionAllowed( Player player, PermitAction action, Location location, 
			List< LocationInstance > locationInstances, HashSet< String > requiredPermits, int id, int data ) {
		
		if ( players.hasPermission( player, "exempt" )) return true;
		// TODO: Errors if no location instances
		if ( locationInstances == null ) return true;
		if ( locationInstances.size() == 0 ) return true;

		if ( requiredPermits.isEmpty()) {
			// DEBUG:
			log.info("No permits exist to restrict " + player.getDisplayName() + " from " + action.asHumanString() + 
					" " + id + ":" + data + " in " + location.getWorld().getName() + " at x" + 
					location.getBlockX() + " y" + location.getBlockY() + " z" + location.getBlockZ());
			return true;
		}
		else {
			// DEBUG:
			String permitDebugList = "";
			for ( String name : requiredPermits )
				permitDebugList += name + " ";
			log.info("checking " + player.getDisplayName() + " for any of these permits: " + permitDebugList );
			return players.hasPermit(player, requiredPermits );
		}
	}
	
	
	@Override
	public boolean onCommand( CommandSender sender, Command cmd, String commandLabel, String[] args ) {
		return Commands.runCommand(sender, cmd, commandLabel, args);
	}
	
	
	private void registerListeners() {
		this.getServer().getPluginManager().registerEvents( new Events( this ), this );
	}
	
	
	/* TODO: Remove
	private void loadEconomy() {
		Server server = getServer();
		if ( server.getPluginManager().getPlugin("Vault") == null ) {
			log.warning("[PermitMe] !! Loading of economy failed, as vault plugin not found");
			return;
		}
		RegisteredServiceProvider< Economy > provider =
			server.getServicesManager().getRegistration( Economy.class );
		if ( provider == null ) {
			log.warning("[PermitMe] !! Loading of economy failed, as vault service provider not registered");
			return;
		}
		economy = provider.getProvider();
	} */
	
	
	private void loadPermissions() {
		Server server = getServer();
		if ( server.getPluginManager().getPlugin("Vault") == null ) {
			log.warning("[PermitMe] !! Loading of permissions failed, as vault plugin not found");
			return;
		}
		RegisteredServiceProvider< Permission > provider =
			server.getServicesManager().getRegistration( Permission.class );
		if ( provider == null ) {
			log.warning("[PermitMe] !! Loading of permissions failed, as vault service provider not registered");
			return;
		}
		permissions = provider.getProvider();		
	}
	
	
	
	
	
	
}
