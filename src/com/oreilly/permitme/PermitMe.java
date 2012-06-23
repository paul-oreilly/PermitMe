package com.oreilly.permitme;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.logging.Logger;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;

import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import com.oreilly.permitme.data.LocationInstance;
import com.oreilly.permitme.data.PermitAction;


// TODO: Add a string translation class and config file, with language support?
// TODO: Add a onLoad method, that registers listeners for PluginEnableEvent and PluginDisableEvent for services
// TODO: Find a way to detect when a plugin has -finished- being enabled!



public class PermitMe extends JavaPlugin {

	public static PermitMe instance = null;
	
	public static Logger log = Logger.getLogger("minecraft");
	public static Economy economy = null;
	public static Permission permissions = null;
	
	public Players players;
	public Permits permits;
	public Locations locations;
	
	public Server server;
	
	
	@Override
	public void onEnable() {
		
		instance = this;
		server = this.getServer();
		
		// start the managers
		players = new Players();
		permits = new Permits();
		locations = new Locations( this );
		
		// load the config file, permits and player info
		Config.load( this );
		
		registerListeners();
		loadEconomy();
		loadPermissions();
		setupCommands();
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
		instance = null;
	}
	
	
	// TODO: Location settings - strict handling
	public boolean isActionAllowed( Player player, PermitAction action, Location location, int id, int data ) {
		if ( players.hasPermission( player, "exempt" )) return true;
		List< LocationInstance > locationInstances = locations.getLocationInstances( location );
		// TODO: Errors if no location instances
		if ( locationInstances == null ) return true;
		if ( locationInstances.size() == 0 ) return true;
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
	
	
	private void registerListeners() {
		this.getServer().getPluginManager().registerEvents( new Events( this ), this );
	}
	
	
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
	}
	
	
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
	
	
	private void setupCommands() {
		// TODO: In-game commands, handler and alias config
	}
	
	
	
}
