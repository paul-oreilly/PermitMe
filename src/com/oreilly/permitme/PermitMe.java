package com.oreilly.permitme;

import java.util.logging.Logger;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;

import org.bukkit.Server;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import com.oreilly.permitme.permit.PermitManager;
import com.oreilly.permitme.player.PlayerManager;

// TODO: Add a string translation class and config file, with language support?
// TODO: Add support for limiting interaction with blocks (eg induction furnace) by extending the item use listener to also block on the ID of the target block


public class PermitMe extends JavaPlugin {

	public static Logger log = Logger.getLogger("minecraft");
	public static Economy economy = null;
	public static Permission permissions = null;
	
	public PlayerManager playerManager;
	public PermitManager permitManager;
	
	
	@Override
	public void onEnable() {
		
		// start the managers
		playerManager = new PlayerManager( this );
		permitManager = new PermitManager( this );
		
		// load the config file, permits and player info
		Config.Load( this );
		
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
		playerManager.save();
		log.info("[PermitMe] Saving permits");
		permitManager.save();
		log.info("[PermitMe] .. save complete");
		log.info("[PermitMe] -> Disabled.");
	}
	
	
	private void registerListeners() {
		this.getServer().getPluginManager().registerEvents( new EventListener( this ), this );
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
