package com.oreilly.permitme;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import com.oreilly.permitme.data.LocationInstance;
import com.oreilly.permitme.data.PermitAction;
import com.oreilly.permitme.events.PermitMeBlockActivationEvent;
import com.oreilly.permitme.events.PermitMeBlockBreakEvent;
import com.oreilly.permitme.events.PermitMeBlockPlaceEvent;
import com.oreilly.permitme.events.PermitMeItemCraftEvent;
import com.oreilly.permitme.events.PermitMeItemUseEvent;
import com.oreilly.permitme.events.PermitMePrepareItemCraftEvent;

//TODO Support for golem construction



public class Events implements Listener {

	private final PermitMe manager;
	
	public static final Integer[] SIGN_ID_LIST = { 63, 68 };

	
	public Events( PermitMe manager ) {
		this.manager = manager;
	}

	
	
	@EventHandler( priority = EventPriority.HIGHEST )
	public void onBlockBreak( BlockBreakEvent event ) {
		
		if ( event.isCancelled()) return;
		
		Player player = event.getPlayer();
		Block block = event.getBlock();
		Location location = block.getLocation();
		int id = block.getTypeId();
		int data = block.getData();
		
		// get data from PermitMe
		List< LocationInstance > locationInstances = 
				PermitMe.instance.locations.getLocationInstances( location );
		HashSet< String > requiredPermits = 
				PermitMe.instance.getRequiredPermits( PermitAction.BLOCKDESTORY, 
						locationInstances, id, data );
		boolean allow = PermitMe.instance.isActionAllowed( player, PermitAction.BLOCKDESTORY, 
				location, locationInstances, requiredPermits, id, data );
		
		// create and fire off event
		PermitMeBlockBreakEvent permitMeEvent = new PermitMeBlockBreakEvent( event, player, 
				locationInstances, requiredPermits, allow );
		PermitMe.instance.getServer().getPluginManager().callEvent( permitMeEvent );
	}
	
	
	@EventHandler( priority = EventPriority.MONITOR )
	public void onPermitMeBlockBreak( PermitMeBlockBreakEvent event ) {
		if ( ! event.allowAction ) {
			event.orignalEvent.setCancelled( true );
			event.player.sendMessage( "You don't have the correct permit to break that" );
		}
	}
	
	
	// TODO: Extend to golem construction checking
	// TODO: Track sign placement, pass to new handler
	// TODO: See how BlockCanBuildEvent is different from BlockPlaceEvent
	@EventHandler( priority = EventPriority.HIGHEST )
	public void onBlockPlace( BlockPlaceEvent event ) {
		
		if ( event.isCancelled()) return;
		
		Player player = event.getPlayer();
		Block block = event.getBlock();
		Location location = block.getLocation();
		int id = block.getTypeId();
		int data = block.getData();
		
		// get data from PermitMe
		List< LocationInstance > locationInstances = 
				PermitMe.instance.locations.getLocationInstances( location );
		HashSet< String > requiredPermits = 
				PermitMe.instance.getRequiredPermits( PermitAction.BLOCKPLACE, 
						locationInstances, id, data );
		boolean allow = PermitMe.instance.isActionAllowed( player, PermitAction.BLOCKPLACE, 
				location, locationInstances, requiredPermits, id, data );
		
		// create and fire off event
		PermitMeBlockPlaceEvent permitMeEvent = new PermitMeBlockPlaceEvent( event, player, 
				locationInstances, requiredPermits, allow );
		PermitMe.instance.getServer().getPluginManager().callEvent( permitMeEvent );
	}
	
	
	@EventHandler( priority = EventPriority.MONITOR )
	public void onPermitMeBlockPlace( PermitMeBlockPlaceEvent event ) {
		if ( ! event.allowAction ) {
			event.orignalEvent.setCancelled( true );
			event.player.sendMessage( "You don't have the correct permit to place that" );
		}
	}
	
	
	
	@EventHandler( priority = EventPriority.HIGHEST )
	public void onPlayerInteract( PlayerInteractEvent event ) {
		// Handles left and right clicks
		//  therefore item use, sign interaction, sign refresh, block activation (use)
		
		// if the event is cancelled, don't do anything
		if ( event.isCancelled()) return;
		
		// get location and other basic information
		Player player = event.getPlayer();
		Action action = event.getAction();
		Block block = event.getClickedBlock();
		ItemStack item = player.getItemInHand();
		Location playerlocation = player.getLocation();
		Location blocklocation = block.getLocation();
		int itemid = item.getTypeId();
		int itemdata = item.getDurability();
		int blockid = block.getTypeId();
		int blockdata = block.getData();
		
		/* TODO: Remove
		// check for sign interactions
		for ( int signID : SIGN_ID_LIST ) {
			if ( block.getTypeId() == signID ) {
				if ( action == Action.LEFT_CLICK_BLOCK ) handleSignRefresh( event );
				if ( action == Action.RIGHT_CLICK_BLOCK ) handleSignInteraction( event );
				break;
			}
		}	*/
		
		// if the player is exempt, don't do anything
		if ( manager.players.hasPermission( player, "exempt")) return;
		
		// check item use....
		// get data from PermitMe
		List< LocationInstance > locationInstances = 
				PermitMe.instance.locations.getLocationInstances( playerlocation );
		HashSet< String > requiredPermits = 
				PermitMe.instance.getRequiredPermits( PermitAction.ITEMUSE, 
						locationInstances, itemid, itemdata );
		boolean allow = PermitMe.instance.isActionAllowed( player, PermitAction.ITEMUSE, 
				playerlocation, locationInstances, requiredPermits, itemid, itemdata );
		// create and fire off event
		PermitMeItemUseEvent permitMeEvent = new PermitMeItemUseEvent( event, player, 
				locationInstances, requiredPermits, allow );
		PermitMe.instance.getServer().getPluginManager().callEvent( permitMeEvent );		
		
		// check block use
		if ( action == Action.RIGHT_CLICK_BLOCK ) {
			locationInstances = PermitMe.instance.locations.getLocationInstances( blocklocation );
			requiredPermits = PermitMe.instance.getRequiredPermits( PermitAction.BLOCKUSE, 
							locationInstances, blockid, blockdata );
			allow = PermitMe.instance.isActionAllowed( player, PermitAction.BLOCKUSE, 
					blocklocation, locationInstances, requiredPermits, blockid, blockdata );
			// create and fire off event
			PermitMeBlockActivationEvent permitMeEvent2 = new PermitMeBlockActivationEvent( event, player, 
					locationInstances, requiredPermits, allow );
			PermitMe.instance.getServer().getPluginManager().callEvent( permitMeEvent2 );			
		}
	}
	
	
	@EventHandler( priority = EventPriority.MONITOR )
	public void onPermitMeItemUse( PermitMeItemUseEvent event ) {
		if ( ! event.allowAction ) {
			event.orignalEvent.setCancelled( true );
			event.player.sendMessage( "You don't have the correct permit to use this item" );
		}
	}
	
	
	@EventHandler( priority = EventPriority.MONITOR )
	public void onPermitMeBlockActivation( PermitMeBlockActivationEvent event ) {
		if ( ! event.allowAction ) {
			event.orignalEvent.setCancelled( true );
			event.player.sendMessage( "You don't have the correct permit to activate that block" );
		}
	}

	
	@EventHandler( priority = EventPriority.HIGHEST )
	public void onCraft( CraftItemEvent event ) {

		if ( event.isCancelled()) return;
		
		Player player = (Player)event.getWhoClicked();
		Location location = player.getLocation();
		ItemStack craftingResult = event.getRecipe().getResult();
		int id = craftingResult.getTypeId();
		int data = craftingResult.getDurability();
		
		// get data from PermitMe
		List< LocationInstance > locationInstances = 
				PermitMe.instance.locations.getLocationInstances( location );
		HashSet< String > requiredPermits = 
				PermitMe.instance.getRequiredPermits( PermitAction.ITEMCRAFT, 
						locationInstances, id, data );
		boolean allow = PermitMe.instance.isActionAllowed( player, PermitAction.ITEMCRAFT, 
				location, locationInstances, requiredPermits, id, data );		
		// create and fire off event
		PermitMeItemCraftEvent permitMeEvent = new PermitMeItemCraftEvent( event, player, 
				locationInstances, requiredPermits, allow );
		PermitMe.instance.getServer().getPluginManager().callEvent( permitMeEvent );			
	}
	
	
	@EventHandler( priority = EventPriority.MONITOR )
	public void onPermitMeItemCraft( PermitMeItemCraftEvent event ) {
		if ( ! event.allowAction ) {
			event.orignalEvent.setCancelled( true );
			event.player.sendMessage( "You don't have the correct permit to craft that item" );
		}
	}

	
	
	@EventHandler( priority = EventPriority.HIGHEST )
	public void onCraftingPrepared( PrepareItemCraftEvent event ) {
		
		// only take action if no players (who have the GUI open) are able to craft the recipe..
		// if any players are exempt, then let it all happen
		
		ItemStack craftingResult = event.getRecipe().getResult();
		int id = craftingResult.getTypeId();
		int data = craftingResult.getDurability();
		List< HumanEntity > viewers = event.getViewers();
		
		// get the location
		InventoryHolder holder = event.getInventory().getHolder();
		Location location = null;
		if ( Entity.class.isInstance( holder )) {
			Entity entity = (Entity)holder;
			location = entity.getLocation();
		} 
		if ( Block.class.isInstance( holder )){
			Block block = (Block)holder;
			location = block.getLocation();
		}
		if ( location == null ) {
			PermitMe.log.warning( "[PermitMe] !! Unable to find cast type for item " + holder.toString() + 
					" (" + holder.getClass().toString() + ") during onCraftingPrepared event" );
			return;
		}
		
		// if no one is watching, that's odd.. but return.
		if (( viewers == null ) | ( viewers.size() == 0 )) {
			PermitMe.log.info("[PermitMe] Crafting prepared event without any players watching? ( " + location + ")" );
			return;
		}
		
		// if any players are exempt, then return
		for ( HumanEntity viewer : viewers ) {
			if ( viewer instanceof Player )
				if ( manager.players.hasPermission( (Player)viewer, "exempt" )) return;
		}
		
		// get data from permitMe
		List< LocationInstance > locationInstances = 
				PermitMe.instance.locations.getLocationInstances( location );
		HashSet< String > requiredPermits = 
				PermitMe.instance.getRequiredPermits( PermitAction.ITEMCRAFT, 
						locationInstances, id, data );
		
		// if any player has a correct permit, then no problem there either...
		boolean allow = false;
		LinkedList< Player> players = new LinkedList< Player >();
		
		for ( HumanEntity viewer : viewers )
			if ( viewer instanceof Player ) {
				players.add( (Player)viewer );
				if ( ! allow )
					allow = PermitMe.instance.isActionAllowed( (Player)viewer, 
							PermitAction.ITEMCRAFT, location, id, data );
			}
		
		// create and fire off event
		PermitMePrepareItemCraftEvent permitMeEvent = new PermitMePrepareItemCraftEvent( event, players, 
				locationInstances, requiredPermits, allow, location );
		PermitMe.instance.getServer().getPluginManager().callEvent( permitMeEvent );
	}
	
	
	@EventHandler( priority = EventPriority.MONITOR )
	public void onPermitMePrepareItemCraft( PermitMePrepareItemCraftEvent event ) {
		if ( ! event.allowAction ) {
		ItemStack craftingResult = event.orignalEvent.getRecipe().getResult();
		int id = craftingResult.getTypeId();
		int data = craftingResult.getDurability();
		// A permit is needed, which no one has.. we'll need some space to throw the items into	
		// find which blocks (in a 3x3 grid) are empty..
		Block center = event.location.getBlock();
		List< Location > emptySpaces = new LinkedList< Location >();
		for ( int x = -1; x < 2; x++ )
			for ( int y = -1; y < 2; y++ )
				for ( int z = -1; z < 2; z++ ) {
					Block neighbour = center.getRelative( x, y, z);
					if ( neighbour.isEmpty() | neighbour.isLiquid())
						emptySpaces.add( neighbour.getLocation());
				}
		// if none at all, then items will end up inside the block above the crafting block
		if ( emptySpaces.size() == 0 ) 
			emptySpaces.add( event.location.add(0, 1, 0));
		// scatter the items 
		boolean removedResult = false;
		CraftingInventory inventory = event.orignalEvent.getInventory();
		for ( ItemStack item : inventory ) {
			if ( item != null ) {
				// see if this is from the "result" slot of the crafting grid
				if ((removedResult == false ) & ( item.getTypeId() == id ) & ( item.getDurability() == data )) {
					// match, so just remove the item (avoids duplication bug)
					inventory.remove( item );
					removedResult = true;
				} else {
					int rand = (int)(Math.random() * emptySpaces.size());
					if ( rand == emptySpaces.size()) rand -= 1;
					Location randomPlacement = emptySpaces.get( rand );
					randomPlacement.getWorld().dropItemNaturally( randomPlacement, item );
					inventory.remove( item );
				}
			}
		}
		// close the open inventory GUI's to avoid graphical glitch
		for ( Player player : event.players )
			player.closeInventory();
		}		
	}
	
	/*
	@EventHandler
	public void onSignChangeEvent( SignChangeEvent event ) {
		// TODO OnSignChangeEvent method
		// Called when text on a sign is changed (or created)
		Player player = event.getPlayer();
		player.sendMessage("Text of sign has changed to " + StringUtils.join( event.getLines(), "|"));
	}


	
	private void handleSignInteraction( PlayerInteractEvent event ) {
		// TODO handleSignInteraction method
		Player player = event.getPlayer();
		BlockState block = event.getClickedBlock().getState();
		
		if ( block instanceof Sign ) {
			Sign sign = (Sign) block;
			PermitMe.log.info("DEBUG: Sign info is " + StringUtils.join( sign.getLines(), "|"));
			int count;
			try { 
				count = Integer.parseInt( sign.getLine(3));
			} catch ( NumberFormatException e ) {
				count = 0;
			}
			sign.setLine(3, Integer.toString(count + 1));
			sign.update( true );
		}
		player.sendMessage("Sign interaction tracked");
		
	}
	
	
	private void handleSignRefresh( PlayerInteractEvent event ) {
		// TODO handleSignRefresh method
		Player player = event.getPlayer();
		player.sendMessage("Sign refresh tracked");
	}
	*/
	

}
