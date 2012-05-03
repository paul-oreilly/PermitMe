package com.oreilly.permitme;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import com.oreilly.permitme.permit.BlockDataRecord;
import com.oreilly.permitme.permit.Permit;
import com.oreilly.permitme.permit.PermitPermission;
import com.oreilly.permitme.permit.PermitRatio;
import com.oreilly.permitme.player.PermitPlayer;



/*
 * The master config file is in the plugins root directory
 * The files describing each permit are in /permits/enabled
 * (Like apache, a this is expected to be symbolic links to files in /permits/available)
 * The files describing the state of each player are in /players
 * 
 */


/*
 * TODO Shop system - data, config, load, save
 * TODO (Low priority!)  consider a file to map "Name" to "ID" to make config files easier to read
 */


public class Config {

	public static boolean enabled;
	
	public static Logger log = Logger.getLogger("minecraft");
	
	public static final File pluginRoot = new File("plugins" + File.separator + "PermitMe");
	public static final File conf = new File( pluginRoot.getPath() + File.separator + "config.yml");
	public static final File permitFolder = new File( pluginRoot.getPath() + File.separator + "permits" + File.separator + "enabled");
	public static final File playerFolder = new File( pluginRoot.getPath() + File.separator + "players");
	
	
	public static void Load( PermitMe manager ) {
		
		FileConfiguration config = loadYamlFile( conf );
		
		enabled = config.getBoolean( ConfigConstant.enabled, true );
		// TODO: Don't process files if not enabled
		log.info("[PermitMe] Loading Permits...");
		
		// Quick check that permit folder also has a peer called "available"
		File availablePermits = new File( permitFolder.getParent() + File.separator + "available" );
		if ( ! availablePermits.exists()) availablePermits.mkdirs();
		
		// TODO: check example files exist
		
		if ( !permitFolder.exists()) {
			permitFolder.mkdirs();
		} else {
			// for each file in the permit folder, make a permit object 
			File[] permitFiles = permitFolder.listFiles();
			if ( permitFiles == null ) {
				log.info("PermitMe !! No permit files found");
			} else {
				for ( int i = 0; i < permitFiles.length; i++ ) {
					// if it doesn't end in ".yml", ignore the file
					if (!permitFiles[i].getName().endsWith(".yml")) continue;
					log.info("[PermitMe] .. Loading permit " + permitFiles[i].getName().substring(0,
							permitFiles[i].getName().indexOf('.')));
					if (!permitFiles[i].canRead()) {
						log.warning("[PermitMe] !! Can't read file: " + permitFiles[i].getName());
						continue;
					}
					// pass the file to a load method
					Permit permit = loadPermit( permitFiles[i]);
					if ( permit != null ) {
						manager.permitManager.addPermit( permit );
						log.info("[PermitMe] .. Permit " + permit.name + " loaded!");
					} else log.warning("[PermitMe] !! Error while processing permit from file " + 
						permitFiles[i].getName());
				}
			}
		}
		
		log.info("[PermitMe] Loading Players...");
		
		if ( !playerFolder.exists()) {
			playerFolder.mkdirs();
		} else {
			// like above, for each file in the player directory...
			File[] playerFiles = playerFolder.listFiles();
			if ( playerFiles == null ) {
				log.info("PermitMe !! No player files found");
			} else {
				for ( int i = 0; i < playerFiles.length; i++ ) {
					if (!playerFiles[i].getName().endsWith(".yml")) continue;
					log.info("[PermitMe] .. Loading player " + playerFiles[i].getName().substring(0,
							playerFiles[i].getName().indexOf('.')));
					if (!playerFiles[i].canRead()) {
						log.warning("[PermitMe] !! Can't read file: " + playerFiles[i].getName());
						continue;
					}
					// pass to a loader function
					PermitPlayer player = loadPlayer( playerFiles[i]);
					if ( player != null ) {
						manager.playerManager.addPlayer( player );
						log.info("[PermitMe] .. Player " + player.name + " loaded!");
					} else log.warning("[PermitMe] !! Error while loading player information from " + 
						playerFiles[i].getName());
				}
			}
		}
		
		log.info("[PermitMe] Loading Complete.");
	}
	
	
	public static Permit loadPermit( File file ) {
		// loads a permit record from a yml file
		
		FileConfiguration config = loadYamlFile( file );
		
		String permitName = config.getString( PermitConstant.name, "" );
		if ( permitName == "" ) return null;
		
		Permit permit = new Permit( permitName, file.getName());
		permit.virtual = config.getBoolean( PermitConstant.virtual, false );
		permit.basePrice = config.getDouble( PermitConstant.basePrice, 10000);
		permit.pricingMethod = config.getString( PermitConstant.pricingMethod, "Simple");
		
		for ( String item : config.getStringList( PermitConstant.permissionBlockBreak ))
			unpackConfigItem( permitName + ":" + PermitConstant.permissionBlockBreak, item, 
				permit.blockBreak, permit.blockBreakMeta );
		
		for ( String item : config.getStringList( PermitConstant.permissionBlockPlace ))
			unpackConfigItem( permitName + ":" + PermitConstant.permissionBlockPlace, item, 
				permit.blockPlace, permit.blockPlaceMeta );
				
		for ( String item : config.getStringList( PermitConstant.permissionBlockUse ))
			unpackConfigItem( permitName + ":" + PermitConstant.permissionBlockUse, item, 
				permit.blockUse, permit.blockUseMeta );		

		for ( String item : config.getStringList( PermitConstant.permissionItemUse ))
			unpackConfigItem( permitName + ":" + PermitConstant.permissionItemUse, item, 
				permit.itemUse, permit.itemUseMeta );
				
		for ( String item : config.getStringList( PermitConstant.permissionItemCraft ))
			unpackConfigItem( permitName + ":" + PermitConstant.permissionItemCraft, item, 
				permit.crafting, permit.craftingMeta );
		
		permit.enchanting = PermitPermission.fromString( 
				config.getString( PermitConstant.permissionItemEnchant, "Undefined"));
		
		permit.golem = PermitPermission.fromString( 
				config.getString( PermitConstant.permissionGolems, "Golem"));
		
		return permit;
	}
	
	
	public static PermitPlayer loadPlayer( File file ) {
		// loads a player record from a yml file
		// the file name is assumed to match the player
		
		FileConfiguration config = loadYamlFile( file );
		String playerName = file.getName().substring(0, file.getName().indexOf('.'));
		PermitPlayer player = new PermitPlayer( playerName );
		
		// add permits
		for ( String permitName : config.getStringList("Permits"))
			if ( permitName != null ) 
				player.permits.add( permitName );
		
		return player;
	}
	
	
	public static void saveConfig() {
		// saves the main config file
		YamlConfiguration config = loadYamlFile( conf );
		config.set( ConfigConstant.enabled, enabled );
		try {
			config.save( conf );
		} catch (IOException e) {
			log.warning("[PermitMe] !! Saving of root config failed");
			e.printStackTrace();
		}
	}
	
	
	public static void savePermit( Permit permit ) {
		// saves a permit into a yml file
		
		File source = new File( permitFolder + File.separator + permit.filename);
		YamlConfiguration config = loadYamlFile( source );
		
		config.set( PermitConstant.name, permit.name);
		config.set( PermitConstant.virtual, permit.virtual );
		config.set( PermitConstant.basePrice, permit.basePrice );
		config.set( PermitConstant.pricingMethod, permit.pricingMethod );
		
		// TODO: Further pricing work
		
		List< String > result = stringRatios( permit );
		config.set( PermitConstant.pricingRatios, permit );
		
		result = concatenateIDs( permit.blockBreak, permit.blockBreakMeta );
		config.set( PermitConstant.permissionBlockBreak, result );
		
		result = concatenateIDs( permit.blockPlace, permit.blockPlaceMeta );
		config.set( PermitConstant.permissionBlockPlace, result );
		
		result = concatenateIDs( permit.blockUse, permit.blockUseMeta );
		config.set( PermitConstant.permissionBlockUse, result );		
		
		result = concatenateIDs( permit.itemUse, permit.itemUseMeta );
		config.set( PermitConstant.permissionItemUse, result );
		
		result = concatenateIDs( permit.crafting, permit.craftingMeta );
		config.set( PermitConstant.permissionItemCraft, result );
		
		config.set( PermitConstant.permissionItemEnchant, permit.enchanting.toString());
		config.set( PermitConstant.permissionGolems, permit.golem.toString());

		try {
			config.save(source);
		} catch (IOException e) {
			log.warning("[PermitMe] !! IO Exception while saving permit " + 
				source.getName() + " to " + source.getPath());
			e.printStackTrace();
		}
	}


	public static void savePlayer( PermitPlayer player ) {
		String playerName = player.name;
		File source = new File( playerFolder + File.separator + playerName + ".yml" );
		YamlConfiguration config = loadYamlFile( source );
		config.set("Permits", player.permits );
		try {
			config.save( source );
		} catch (IOException e) {
			log.warning("[PermitMe] error while saving information for player " + 
				playerName + " to " + source.getAbsolutePath());
			e.printStackTrace();
		}
	}
	
	
	private static List< String > concatenateIDs( List< Integer > simpleIDs, BlockDataRecord complexIDs ) {
		List< String > result = new LinkedList<String>();
		
		for ( Integer i : simpleIDs ) 
			result.add( i.toString());
		
		for ( Integer id : complexIDs.keySet())
			for ( Integer data : complexIDs.get( id ))
				result.add( id.toString() + ":" + data.toString());

		//return result.toArray( new String[0]);
		return result;
	}
	
	
	private static List<String> stringRatios(Permit permit) {
		List< String > result = new LinkedList< String >();
		List< PermitRatio > queue = new LinkedList< PermitRatio >();
		if ( permit.ratios.size() == 0 ) {
			queue.add( PermitRatio.example());
		}
		for ( PermitRatio p : queue )
			result.add( p.toString());
		return result;
	}
	
	
	private static void unpackConfigItem(String identity, String item, LinkedList<Integer> simple, BlockDataRecord complex ) {
		int id; int data;
		String[] split = item.split(":");
		switch( split.length ) {
		case 1: {
			id = Integer.parseInt( split[0] );
			simple.add( id );
			return;
		}
		case 2: {
			id = Integer.parseInt( split[0]);
			data = Integer.parseInt( split[1]);
			complex.addRecord(id, data);
			return;
		}
		default:
			log.warning("[PermitMe] Misconfigured item in " + identity );
		}
	}
	
	
	private static YamlConfiguration loadYamlFile( File file ) {
		if ( !file.exists()) {
			try {
				System.out.print("**** Files doesn't exist, creating.. *****");
				log.warning("[PermitMe] !! File " + file.getName() + " not found.");
				File parent = file.getParentFile();
				log.warning("[PermitMe]   checking directory exists " + parent.getName());
				if ( file.getParentFile().exists() == false ) parent.mkdirs();
				log.warning("[PermitMe]   creating new file " + file.getName());
				file.createNewFile();
			} catch (IOException e) {
				log.warning("[PermitMe] !! IO Error while trying to load " + 
					file.getName() + " from " + file.getPath());
				e.printStackTrace();
			}
		}
		YamlConfiguration config = new YamlConfiguration();
		try {
			config.load( file );
		} catch (FileNotFoundException e) {
			log.warning("[PermitMe] !! File not found error while trying to load " + 
					file.getName() + " from " + file.getPath() + " into config object");
			e.printStackTrace();
		} catch (IOException e) {
			log.warning("[PermitMe] !! IO Error while trying to load " + 
					file.getName() + " from " + file.getPath() + " into config object");
			e.printStackTrace();
		} catch (InvalidConfigurationException e) {
			log.warning("[PermitMe] !! Error: Invalid configuration in file " + 
					file.getName() + " from " + file.getPath());			
			e.printStackTrace();
		}
		return config;
	}
}




class ConfigConstant {
	static public final String enabled = "enabled";
}

class PermitConstant {
	static public final String name = "name";
	static public final String virtual = "virtual";
	static public final String inherits = "inherits";
	static public final String basePrice = "pricing.basePrice";
	static public final String pricingMethod = "pricing.method";
	static public final String pricingRatios = "pricing.ratios";
	static public final String pricingFactorCurrentPrice = "pricing.factor.currentPrice";
	static public final String pricingFactorPurchase = "pricing.factor.onPurchase";
	static public final String pricingFactorDecay = "pricing.factor.onDecay";
	static public final String pricingDecayMethod = "pricing.factor.decay.method";
	static public final String pricingFactorDecayTimeSettig = "pricing.factor.decay.time";
	static public final String permissionBlockBreak = "permission.blockBreaking";
	static public final String permissionBlockPlace = "permission.blockPlacing";
	static public final String permissionBlockUse = "permission.blockActivating";
	static public final String permissionItemUse = "permission.itemUse";
	static public final String permissionItemCraft = "permission.itemCraft";
	static public final String permissionItemEnchant = "permission.itemEnchant";
	static public final String permissionGolems = "permission.golemConstruction";
}




