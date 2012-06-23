package com.oreilly.permitme;

import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.entity.Entity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import com.oreilly.permitme.data.PermitAction;

//TODO Support for golem construction



public class Events implements Listener {

	private final PermitMe manager;
	
	public static final Integer[] SIGN_ID_LIST = { 63, 68 };

	
	public Events( PermitMe manager ) {
		this.manager = manager;
	}

	
	
	@EventHandler
	public void onBlockBreak( BlockBreakEvent event ) {
		
		if ( event.isCancelled()) return;
		
		Player player = event.getPlayer();
		Block block = event.getBlock();
		Location location = block.getLocation();
		int id = block.getTypeId();
		int data = block.getData();
		
		if ( ! manager.isActionAllowed(player, PermitAction.BLOCKDESTORY, location, id, data )) {
			event.setCancelled( true );
			player.sendMessage( "You don't have the correct permit to break that" );
		}
	}
	
	
	// TODO: Extend to golem construction checking
	// TODO: Track sign placement, pass to new handler
	// TODO: See how BlockCanBuildEvent is different from BlockPlaceEvent
	@EventHandler
	public void onBlockPlace( BlockPlaceEvent event ) {
		
		if ( event.isCancelled()) return;
		
		Player player = event.getPlayer();
		Block block = event.getBlock();
		Location location = block.getLocation();
		int id = block.getTypeId();
		int data = block.getData();
		
		if ( ! manager.isActionAllowed(player, PermitAction.BLOCKPLACE, location, id, data )) {
			event.setCancelled( true );
			player.sendMessage( "You don't have the correct permit to place that" );
		}
	}
	
	
	
	@EventHandler
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
		
		// check for sign interactions
		for ( int signID : SIGN_ID_LIST ) {
			if ( block.getTypeId() == signID ) {
				if ( action == Action.LEFT_CLICK_BLOCK ) handleSignRefresh( event );
				if ( action == Action.RIGHT_CLICK_BLOCK ) handleSignInteraction( event );
				break;
			}
		}	
		
		// if the player is exempt, don't do anything
		if ( manager.players.hasPermission( player, "exempt")) return;
		
		// check item use
		if ( ! manager.isActionAllowed(player, PermitAction.ITEMUSE, playerlocation, itemid, itemdata )) {
			event.setCancelled( true );
			player.sendMessage( "You don't have the correct permit to use this item" );
			return;
		}		
		
		// check block use
		if ( action == Action.RIGHT_CLICK_BLOCK ) {
			if ( ! manager.isActionAllowed(player, PermitAction.BLOCKUSE, blocklocation, blockid, blockdata )) {
				event.setCancelled( true );
				player.sendMessage( "You don't have the correct permit to activate that block" );
			}		
		}
	}

	
	@EventHandler
	public void onCraft( CraftItemEvent event ) {

		if ( event.isCancelled()) return;
		
		Player player = (Player)event.getWhoClicked();
		Location location = player.getLocation();
		ItemStack craftingResult = event.getRecipe().getResult();
		int id = craftingResult.getTypeId();
		int data = craftingResult.getDurability();

		if ( ! manager.isActionAllowed(player, PermitAction.ITEMCRAFT, location, id, data )) {
			event.setCancelled( true );
			player.sendMessage( "You don't have the correct permit to craft that item" );
		}				
	}
	

	
	
	@EventHandler
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
			
		// if any player has a correct permit, then no problem there either...
		for ( HumanEntity viewer : viewers )
			if ( viewer instanceof Player )
				if ( manager.isActionAllowed((Player)viewer, PermitAction.ITEMCRAFT, location, id, data )) return;
				
		// A permit is needed, which no one has.. we'll need some space to throw the items into	
		// find which blocks (in a 3x3 grid) are empty..
		Block center = location.getBlock();
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
			emptySpaces.add( location.add(0, 1, 0));
		// scatter the items 
		boolean removedResult = false;
		CraftingInventory inventory = event.getInventory();
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
		for ( HumanEntity viewer : viewers ) {
			if ( Player.class.isInstance( viewer )) {
				Player player = (Player)viewer;
				player.closeInventory();
			}
		}		
	}
	
	
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
	
	

}
