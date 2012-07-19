package com.oreilly.permitme;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import com.oreilly.permitme.data.BlockDataRecord;
import com.oreilly.permitme.data.TFU;
import com.oreilly.permitme.record.LocationRecord;
import com.oreilly.permitme.record.LocationTemplate;
import com.oreilly.permitme.record.PermitPlayer;
import com.oreilly.permitme.record.SavedPermit;



/*
 * The master config file is in the plugins root directory
 * The files describing each permit are in /permits/enabled
 * (Like apache, a this is expected to be symbolic links to files in /permits/available)
 * The files describing the state of each player are in /players
 * 
 */



public class Config {

	public static boolean enabled;
	
	public static File pluginRoot = null;
	public static File conf = null;
	public static File permitFolder = null;
	public static File playerFolder = null;
	public static File locationRecordFolder = null;
	public static File locationTemplateFolder = null;
	
	public static boolean permitsLoaded = false;
	public static boolean locationsLoaded = false;
	public static boolean playersLoaded = false;

	
	public static void load( PermitMe manager ) {
		
		loadFilePaths();
		
		FileConfiguration config = loadYamlFile( conf );
		enabled = config.getBoolean( ConfigConstant.enabled, true );
		
		if ( enabled ) {
			
			if ( Settings.debugMode ) 
				PermitMe.log.info("[PermitMe] DEBUG: Starting to load configuration");
			
			// load permits, and call config complete to assign any missing uuid's
			loadPermits( manager );
			manager.permits.ConfigComplete();
			permitsLoaded = true;
			
			// load locations
			loadLocations( manager );
			manager.locations.loadingComplete();
			locationsLoaded = true;
			
			// load players 
			loadPlayers( manager );
			playersLoaded = true;
			
			PermitMe.log.info("[PermitMe] Loading Complete.");
			
		} else
			PermitMe.log.warning("[PermitMe] Plugin disabled - files will not be loaded.");
	}
	
	
	private static void loadFilePaths() {
		pluginRoot = PermitMe.instance.getDataFolder();
		conf = new File( pluginRoot.getPath() + File.separator + "config.yml");
		permitFolder = new File( pluginRoot.getPath() + File.separator + "permits" + File.separator + "enabled");
		playerFolder = new File( pluginRoot.getPath() + File.separator + "players");
		locationRecordFolder = new File( pluginRoot.getPath() + File.separator + "locations" );
		locationTemplateFolder = new File( pluginRoot.getPath() + File.separator + "templates" );		
	}
	
	
	private static void loadPermits( PermitMe manager ) {
		
		if ( Settings.debugMode ) 
			PermitMe.log.info("[PermitMe] Loading permit information");
		
		// Quick check that permit folder also has a peer called "available"
		File availablePermits = new File( permitFolder.getParent() + File.separator + "available" );
		if ( ! availablePermits.exists()) availablePermits.mkdirs();
		
		if ( !permitFolder.exists()) {
			permitFolder.mkdirs();
		} else {
			// for each file in the permit folder, make a permit object 
			File[] permitFiles = permitFolder.listFiles();
			if ( permitFiles == null ) {
				PermitMe.log.info("[PermitMe] No permit files found in " + permitFolder.getAbsolutePath());
			} else {
				for ( int i = 0; i < permitFiles.length; i++ ) {
					// if it doesn't end in ".yml", ignore the file
					if (!permitFiles[i].getName().endsWith(".yml")) continue;
					if (!permitFiles[i].canRead()) {
						PermitMe.log.warning("[PermitMe] Unable to read file contents of " + permitFiles[i].getName());
						continue;
					}
					// pass the file to a load method
					SavedPermit permit = loadPermit( permitFiles[i]);
					if ( permit != null ) {
						manager.permits.addPermit( permit );
						if ( Settings.debugMode ) 
							PermitMe.log.info("[PermitMe] .. Permit " + permit.name + " loaded!");
					} else PermitMe.log.warning("[PermitMe] Error while processing permit from file " + 
						permitFiles[i].getName() + " (null return)");
				}
			}
		}
	}
	
	
	private static void loadLocations( PermitMe manager ) {
		// load location templates
		if ( Settings.debugMode ) 
			PermitMe.log.info("[PermitMe] Loading location templates...");
		if ( !locationTemplateFolder.exists()) 
			locationTemplateFolder.mkdirs();
		File[] locationFiles = locationTemplateFolder.listFiles();
		if ( locationFiles == null ) PermitMe.log.info("[PermitMe] No location templates found in " + 
				locationTemplateFolder.getAbsolutePath());
		else {
			for ( File f : locationFiles ) {
				if ( ! f.getName().endsWith(".yml")) continue;	
				if ( ! f.canRead()) {
					PermitMe.log.warning("[PermitMe] File " + f.getAbsolutePath() + " is unreadable.");
					continue;
				}
				LocationTemplate template = loadLocationTemplate( f );
				if ( template == null )
					PermitMe.log.warning("[PermitMe] Error while loading file " + f.getName());
				else {
					manager.locations.addLocationTemplate( template );
					if ( Settings.debugMode ) PermitMe.log.info("[PermitMe] Loading of template " + 
							f.getName() + " complete" );
				}
			}
		}
		// load location records
		if ( Settings.debugMode ) 
			PermitMe.log.info("[PermitMe] Loading location records...");
		if ( !locationRecordFolder.exists()) locationRecordFolder.mkdirs();
		locationFiles = locationRecordFolder.listFiles();
		if ( locationFiles == null ) 
				PermitMe.log.info("[PermitMe] No location records found in " + 
				locationRecordFolder.getAbsolutePath());
		else {
			for ( File f : locationFiles ) {
				if ( ! f.getName().endsWith(".yml")) continue;	
				if ( ! f.canRead()) {
					PermitMe.log.warning("[PermitMe] Unable to read file data");
					continue;
				}
				List< LocationRecord > records = loadLocationRecord( f );
				if ( records == null )
					PermitMe.log.warning("[PermitMe] Error wihle loading file " + f.getName());
				else {
					manager.locations.addLocationRecords( records );
					PermitMe.log.info("[PermitMe] Loading of record " + f.getName() + " complete");
				}
			}
		}
	}

	
	private static void loadPlayers( PermitMe manager ) {
		PermitMe.log.info("[PermitMe] Loading Players...");
		if ( !playerFolder.exists()) {
			playerFolder.mkdirs();
		} else {
			// like above, for each file in the player directory...
			File[] playerFiles = playerFolder.listFiles();
			if ( playerFiles == null ) {
				PermitMe.log.info("PermitMe !! No player files found");
			} else {
				for ( int i = 0; i < playerFiles.length; i++ ) {
					if (!playerFiles[i].getName().endsWith(".yml")) continue;
					if (!playerFiles[i].canRead()) {
						PermitMe.log.warning("[PermitMe] !! Can't read file: " + playerFiles[i].getName());
						continue;
					}
					// pass to a loader function
					PermitPlayer player = loadPlayer( playerFiles[i]);
					if ( player != null ) {
						manager.players.addPlayer( player );
						PermitMe.log.info("[PermitMe] .. Player " + player.name + " loaded!");
					} else PermitMe.log.warning("[PermitMe] !! Error while loading player information from " + 
						playerFiles[i].getName());
				}
			}
		}
	}
	
	
	private static SavedPermit loadPermit( File file ) {
		// loads a permit record from a yml file
		
		FileConfiguration config = loadYamlFile( file );
		
		if ( Settings.debugMode )
			PermitMe.log.info("[PermitMe] Loading permit from " + file.getAbsolutePath());
		
		String signName = config.getString( PermitConstant.signName, "" );
		if ( signName == "" ) {
			PermitMe.log.warning("[PermitMe] The permit file " + file.getAbsolutePath() + 
					" does not have a name for the permit" );
			return null;
		}
		String UUID = config.getString( PermitConstant.UUID, null );

		SavedPermit permit = new SavedPermit( signName, UUID, file.getName());
		permit.virtual = config.getBoolean( PermitConstant.virtual, false );
		permit.inheritenceAsStrings = config.getStringList( PermitConstant.inheritsAsStrings );
		
		// load pricing information
		/* TODO: Remove
		permit.basePrice = config.getDouble( PermitConstant.basePrice, 10000);
		permit.pricingMethod = PermitPricingMethod.fromString( 
				config.getString( PermitConstant.pricingMethod, "Simple"),
				"file " + permit.filename );
		permit.pricingRatios = PermitRatio.fromStrings(
				config.getStringList( PermitConstant.pricingRatios ));
		permit.pricingFactorCurrentPrice = config.getDouble( 
				PermitConstant.pricingFactorCurrentPrice, 10000 );
		permit.pricingFactorOnPurchase = config.getDouble(
				PermitConstant.pricingFactorOnPurchase, 2 );
		permit.pricingFactorOnDecay = config.getDouble(
				PermitConstant.pricingFactorDecay, 0.5 );
		permit.pricingDecayMethod = PermitPriceDecayMethod.fromString(
				config.getString( PermitConstant.pricingDecayMethod, "time" ), 
				"file " + permit.filename );
		permit.pricingDecayTime = config.getLong(
				PermitConstant.pricingDecayTime, 3600 ); */
		
		// load permission information
		
		for ( String item : config.getStringList( PermitConstant.permissionBlockBreak ))
			unpackConfigItem( signName + ":" + PermitConstant.permissionBlockBreak, item, 
				permit.blockBreak, permit.blockBreakComplex );
		
		for ( String item : config.getStringList( PermitConstant.permissionBlockPlace ))
			unpackConfigItem( signName + ":" + PermitConstant.permissionBlockPlace, item, 
				permit.blockPlace, permit.blockPlaceComplex );
				
		for ( String item : config.getStringList( PermitConstant.permissionBlockUse ))
			unpackConfigItem( signName + ":" + PermitConstant.permissionBlockUse, item, 
				permit.blockUse, permit.blockUseComplex );		

		for ( String item : config.getStringList( PermitConstant.permissionItemUse ))
			unpackConfigItem( signName + ":" + PermitConstant.permissionItemUse, item, 
				permit.itemUse, permit.itemUseComplex );
				
		for ( String item : config.getStringList( PermitConstant.permissionItemCraft ))
			unpackConfigItem( signName + ":" + PermitConstant.permissionItemCraft, item, 
				permit.crafting, permit.craftingComplex );
				
		if ( Settings.debugMode ) {
			PermitMe.log.info("[PermitMe] Loading of permit " + permit.name + " complete");
			if ( Settings.debugPermitContents )
				PermitMe.log.info(permit.toHumanString());
		}
		
		return permit;
	}
	
	
	private static LocationTemplate loadLocationTemplate( File f ) {
		// templates are one to a file
		FileConfiguration config = loadYamlFile( f );
		
		if ( Settings.debugMode )
			PermitMe.log.info("[PemitMe] Loading template from file " + f.getAbsolutePath());
		
		String name = config.getString( LocationTemplateConstant.name );
		if ( name == null ) return null;
		
		LocationTemplate template = new LocationTemplate( name );
		
		if ( config.contains( LocationTemplateConstant.isolated ))
			template.isolation = TFU.fromBoolean( config.getBoolean( LocationTemplateConstant.isolated ));
		else template.isolation = TFU.UNDEFINED;
		
		if ( config.contains( LocationTemplateConstant.strict ))
			template.strictMode = TFU.fromBoolean( config.getBoolean( LocationTemplateConstant.strict ));
		else template.strictMode = TFU.UNDEFINED;
		
		if ( config.contains( LocationTemplateConstant.singleRole ))
			template.singleRole = TFU.fromBoolean( config.getBoolean( LocationTemplateConstant.singleRole ));
		else template.singleRole = TFU.UNDEFINED;
		
		template.rawPermitUIDList = config.getStringList( LocationTemplateConstant.permitUIDList );
		
		if ( Settings.debugMode ) {
			PermitMe.log.info("[PermitMe] Template loading completed for " + name );
			if ( Settings.debugLocationTemplateContents )
				PermitMe.log.info( template.toHumanString());
		}
		
		return template;
	}
	
	
	private static List< LocationRecord > loadLocationRecord( File f ) {
		// location records are stored by [world]-[type].yml (and just [world].yml for worlds)
		//  either way, the contents of the file is the same - a list of one or more location records
		
		List< LocationRecord > result = new LinkedList< LocationRecord >();
		FileConfiguration config = loadYamlFile( f );
		String standardWarning = "[PermitMe] Malformed location record in " + f.getName() + "::";
		
		if ( Settings.debugMode )
			PermitMe.log.info("[PermitMe] Loading location record from " + f.getAbsolutePath());
		
		Set< String > keys = config.getKeys( false );
		for ( String key : keys ) {
			ConfigurationSection section = config.getConfigurationSection( key );
			if ( section == null ) continue;
			/*  DEBUG:
			for ( String str : section.getKeys(false ))
				PermitMe.log.info("[PermitMe] DEBUG: in record " + key + " from " + f.getAbsolutePath() + " a key is [" + str + "]" );
			// cont. */
			String name = section.getString( LocationRecordConstant.name );
			String world = section.getString( LocationRecordConstant.world );
			String type = section.getString( LocationRecordConstant.type );
			List< String > settings = section.getStringList( LocationRecordConstant.settings );
			// DEBUG:
			//PermitMe.log.info("Settings are: " + settings );
			// check that we have all required data
			if ( name == null ) {
				PermitMe.log.warning( standardWarning + key + ". No name provided." );
				continue;
			}
			if ( type == null ) {
				PermitMe.log.warning( standardWarning + key + ". No type provided." );
				continue;
			}
			// world can be omitted if type is "world" (world is then set to "name")
			if ( type.contentEquals( "world" )) 
				world = name;
			if ( world == null ) {
				PermitMe.log.warning( standardWarning + key + ". No world provided." );
				continue;				
			}
			if (( settings == null ) | ( settings.size() == 0 )) {
				PermitMe.log.warning( standardWarning + key + ". No settings provided." );
				continue;					
			}
			LocationRecord record = new LocationRecord( name, type, world, settings );
			result.add( record );
		}
		
		if ( Settings.debugMode ) {
			PermitMe.log.info("[PermitMe] loading of location record file " + f.getAbsolutePath() + " complete");
			if ( Settings.debugLocationRecordContents )
				for ( LocationRecord record : result )
					PermitMe.log.info( record.toHumanString());
		}
		
		return result;
	}
	
	
	private static PermitPlayer loadPlayer( File file ) {
		// loads a player record from a yml file
		// the file name is assumed to match the player
		
		FileConfiguration config = loadYamlFile( file );
		
		if ( Settings.debugMode )
			PermitMe.log.info("[PermitMe] Loading player information from " + file.getAbsolutePath());
		
		String playerName = config.getString( PlayerConstant.name );
		PermitPlayer player = new PermitPlayer( playerName );
		
		// add permits
		for ( String permitName : config.getStringList( PlayerConstant.permits ))
			if ( permitName != null ) 
				PermitMe.instance.players.addPermit( permitName, player );
				//player.permits.add( permitName );
		
		if ( Settings.debugMode ) {
			PermitMe.log.info("[PermitMe] Loading of player " + playerName + " complete");
			if ( Settings.debugPlayerContents )
				PermitMe.log.info( player.toHumanString());
		}
				
		return player;
	}
	
	
	public static void saveConfig() {
		// saves the main config file
		YamlConfiguration config = loadYamlFile( conf );
		config.set( ConfigConstant.enabled, enabled );
		try {
			config.save( conf );
		} catch (IOException e) {
			PermitMe.log.warning("[PermitMe] !! Saving of root config failed");
			e.printStackTrace();
		}
	}
	
	
	public static void savePermit( SavedPermit permit ) {
		// saves a permit into a yml file
		
		File source = new File( permitFolder + File.separator + permit.filename);
		YamlConfiguration config = loadYamlFile( source );
		
		config.set( PermitConstant.signName, permit.name);
		config.set( PermitConstant.UUID, permit.UUID );
		config.set( PermitConstant.virtual, permit.virtual );
		config.set( PermitConstant.inheritsAsStrings, permit.inheritenceAsStrings.toArray());
		
		// save pricing information TODO: Remove
		/*
		config.set( PermitConstant.pricingMethod, permit.pricingMethod.toString());
		config.set( PermitConstant.basePrice, permit.basePrice );
		
		config.set( PermitConstant.pricingRatios, PermitRatio.toStrings( permit.pricingRatios ));
		
		config.set( PermitConstant.pricingFactorCurrentPrice, 
			permit.pricingFactorCurrentPrice );
		config.set( PermitConstant.pricingFactorOnPurchase, 
			permit.pricingFactorOnPurchase );
		config.set( PermitConstant.pricingFactorDecay, 
			permit.pricingFactorOnDecay );
		config.set( PermitConstant.pricingDecayMethod,
			permit.pricingDecayMethod.toString());
		config.set( PermitConstant.pricingDecayTime,
			permit.pricingDecayTime );
		*/
		// save permissions
		
		List< String > result = concatenateIDs( permit.blockBreak, permit.blockBreakComplex );
		config.set( PermitConstant.permissionBlockBreak, result );
		
		result = concatenateIDs( permit.blockPlace, permit.blockPlaceComplex );
		config.set( PermitConstant.permissionBlockPlace, result );
		
		result = concatenateIDs( permit.blockUse, permit.blockUseComplex );
		config.set( PermitConstant.permissionBlockUse, result );		
		
		result = concatenateIDs( permit.itemUse, permit.itemUseComplex );
		config.set( PermitConstant.permissionItemUse, result );
		
		result = concatenateIDs( permit.crafting, permit.craftingComplex );
		config.set( PermitConstant.permissionItemCraft, result );
		
		/*if ( permit.enchanting != null )
			config.set( PermitConstant.permissionItemEnchant, permit.enchanting.toString());
		
		if ( permit.golem != null )
			config.set( PermitConstant.permissionGolems, permit.golem.toString());
*/
		try {
			config.save(source);
		} catch (IOException e) {
			PermitMe.log.warning("[PermitMe] !! IO Exception while saving permit " + 
				source.getName() + " to " + source.getPath());
			e.printStackTrace();
		}
	}


	public static void savePlayer( PermitPlayer player ) {
		String playerName = player.name;
		File source = new File( playerFolder + File.separator + playerName + ".yml" );
		YamlConfiguration config = loadYamlFile( source );
		config.set( PlayerConstant.name, player.name );
		// Since we can't save 'sets', we need to convert permits to a list 
		LinkedList< String > playerPermits = new LinkedList< String >();
		playerPermits.addAll( player.permits );
		config.set( PlayerConstant.permits, playerPermits );
		try {
			config.save( source );
		} catch (IOException e) {
			PermitMe.log.warning("[PermitMe] error while saving information for player " + 
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
			PermitMe.log.warning("[PermitMe] Misconfigured item in " + identity );
		}
	}
	
	
	private static YamlConfiguration loadYamlFile( File file ) {
		if ( !file.exists()) {
			try {
				System.out.print("**** Files doesn't exist, creating.. *****");
				PermitMe.log.warning("[PermitMe] !! File " + file.getName() + " not found.");
				File parent = file.getParentFile();
				PermitMe.log.warning("[PermitMe]   checking directory exists " + parent.getName());
				if ( file.getParentFile().exists() == false ) parent.mkdirs();
				PermitMe.log.warning("[PermitMe]   creating new file " + file.getName());
				file.createNewFile();
			} catch (IOException e) {
				PermitMe.log.warning("[PermitMe] !! IO Error while trying to load " + 
					file.getName() + " from " + file.getPath());
				e.printStackTrace();
			}
		}
		YamlConfiguration config = new YamlConfiguration();
		try {
			config.load( file );
		} catch (FileNotFoundException e) {
			PermitMe.log.warning("[PermitMe] !! File not found error while trying to load " + 
					file.getName() + " from " + file.getPath() + " into config object");
			e.printStackTrace();
		} catch (IOException e) {
			PermitMe.log.warning("[PermitMe] !! IO Error while trying to load " + 
					file.getName() + " from " + file.getPath() + " into config object");
			e.printStackTrace();
		} catch (InvalidConfigurationException e) {
			PermitMe.log.warning("[PermitMe] !! Error: Invalid configuration in file " + 
					file.getName() + " from " + file.getPath());			
			e.printStackTrace();
		}
		return config;
	}
	
}





class ConfigConstant {
	static public final String enabled = "enabled";
}


class PlayerConstant {
	static public final String name = "name";
	static public final String permits = "permits";
}


class PermitConstant {
	static public final String signName = "permitName";
	static public final String UUID = "UUID";
	static public final String virtual = "virtual";
	static public final String inheritsAsStrings = "inherits";/*
	static public final String basePrice = "pricing.basePrice"; 
	static public final String pricingMethod = "pricing.method";
	static public final String pricingRatios = "pricing.ratios";
	static public final String pricingFactorCurrentPrice = "pricing.factor.currentPrice";
	static public final String pricingFactorOnPurchase = "pricing.factor.onPurchase";
	static public final String pricingFactorDecay = "pricing.factor.onDecay";
	static public final String pricingDecayMethod = "pricing.factor.decayMethod";
	static public final String pricingDecayTime = "pricing.factor.decayTime"; */
	static public final String permissionBlockBreak = "allows.blockBreaking";
	static public final String permissionBlockPlace = "allows.blockPlacing";
	static public final String permissionBlockUse = "allows.blockUse";
	static public final String permissionItemUse = "allows.itemUse";
	static public final String permissionItemCraft = "allows.itemCraft";
	static public final String permissionItemEnchant = "allows.itemEnchant";
	static public final String permissionGolems = "allows.golemConstruction";
}


class LocationTemplateConstant {
	static public final String isolated = "isolated";
	static public final String name = "name";
	static public final String singleRole = "singleRoleOnly";
	static public final String strict = "strictMode";
	static public final String permitUIDList = "permits";
}


class LocationRecordConstant {
	static public final String name = "name";
	static public final String type = "type";
	static public final String world = "world";
	static public final String settings = "settings";
}


