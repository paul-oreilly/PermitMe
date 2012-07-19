package com.oreilly.permitme;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;

import org.apache.commons.lang.StringUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

class CommandGroup {
	public HashMap< String, CommandGroup > children = new HashMap< String, CommandGroup >();
	public HashMap< Integer, Commands > commandsByArgCount = new HashMap< Integer, Commands >();
	
	public CommandGroup() {}
	
}


abstract public class Commands {
	
	static public HashMap< String, CommandGroup > commandList = 
			new HashMap< String, CommandGroup >();
	static public LinkedList< Commands > allCommands = new LinkedList< Commands >();
	
	static public boolean runCommand( CommandSender sender, Command cmd, String commandLabel, String[] args ) {
		if ( PermitMe.instance == null ) {
			sender.sendMessage("PermitMe is not loaded");
			return false;
		}
		LinkedList< String > arguements = new LinkedList< String >( Arrays.asList( args ));
		arguements.add(0, cmd.getName());
		CommandGroup currentGroup = null;
		HashMap< String, CommandGroup > currentHash = commandList;
		Commands command = null;
		int arguementsLeft = arguements.size();
		for ( String arguement : arguements ) {
			arguement = arguement.toLowerCase().trim();
			if ( Settings.debugMode )
				if ( Settings.debugCommandExecution ) {
					PermitMe.log.info("[PermitMe] DEBUG: Checking execution tree for " + arguement );
					PermitMe.log.info("[PermitMe] DEBUG: Current options are " + StringUtils.join( currentHash.keySet(), ","));
				}
			currentGroup = currentHash.get( arguement );
			if ( currentGroup == null ) {
				if ( Settings.debugMode )
					if ( Settings.debugCommandExecution )
						sender.sendMessage("Command failed when a command group \nfor " + arguement + " was not found");
				return false;
			}
			currentHash = currentGroup.children;
			arguementsLeft--;
			command = currentGroup.commandsByArgCount.get( arguementsLeft );
			if ( command != null ) {
				arguements.removeAll( Arrays.asList( command.matchingSequence ));
				String[] data = arguements.toArray( new String[ command.dataArgs ]);
				if ( Settings.debugMode )
					if ( Settings.debugCommandExecution )
						sender.sendMessage("Running command with arguements " + StringUtils.join( data, ","));
				return command.run(sender, cmd, commandLabel, data );
			}
		}
		return false;
	}
	
	static public void loadCommands() {
		new PlayerAddPermit( "permitme","player","add","permit" );
		new PlayerRemovePermit( "permitme","player","remove","permit" );
		
		// show debug info if required:
		if ( Settings.debugMode )
			if ( Settings.debugCommandCreation )
				for ( Commands command : allCommands )
					PermitMe.log.info("[PermitMe] Loaded command with " + command.dataArgs + 
							" arguements: " + StringUtils.join( command.matchingSequence, ","));
	}
	
	public int dataArgs = 0;
	public int totalArgs = 0;
	public String[] matchingSequence = null;
	
	public Commands( String[] matchingSequence, int dataArgs ) {
		this.matchingSequence = matchingSequence;
		this.dataArgs = dataArgs;
		this.totalArgs = dataArgs + matchingSequence.length;
		allCommands.add( this );
		// register command
		HashMap< String, CommandGroup > currentHash = commandList;
		CommandGroup currentGroup = null;
		for ( String sequence : matchingSequence ) {
			sequence = sequence.toLowerCase().trim();
			currentGroup = currentHash.get( sequence );
			if ( currentGroup == null ) {
				currentGroup = new CommandGroup();
				currentHash.put( sequence, currentGroup );
			}
			currentHash = currentGroup.children;
		}
		if ( currentGroup.commandsByArgCount.get( dataArgs ) != null )
			PermitMe.log.warning("[PermitMe] Command clash - a command with arguements " + 
					matchingSequence + " and " + dataArgs + " arguements already exists");
		else
			currentGroup.commandsByArgCount.put( dataArgs, this );
	}
	
	abstract public boolean run( CommandSender sender, Command cmd, String commandLabel, String[] data );
}


class PlayerAddPermit extends Commands {
	public PlayerAddPermit( String... sequence ) {
		super( sequence, 2 );
	}
	@Override
	public boolean run( CommandSender sender, Command cmd, String commandLabel, String[] data ) {
		boolean success = PermitMe.instance.addPermitToPlayer( data[0], data[1] );
		if ( success ) 
			sender.sendMessage("Added permit " + data[1] + " to player " + data[0]);
		else
			sender.sendMessage("Failed to add permit " + data[1] + " to player " + data[0]);
		return success;
	}
}

class PlayerRemovePermit extends Commands {
	public PlayerRemovePermit( String... sequence ) {
		super( sequence, 2 );
	}
	@Override
	public boolean run( CommandSender sender, Command cmd, String commandLabel, String[] data ) {
		boolean success = PermitMe.instance.removePermitFromPlayer( data[0], data[1] );
		if ( success )
			sender.sendMessage("Removed permit " + data[1] + " from player " + data[0]);
		else
			sender.sendMessage("Failed to remove permit " + data[1] + " from player " + data[0]);
		return success;
	}
}
