package com.oreilly.permitme;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import com.oreilly.permitme.permit.PermitManager;


//TODO Support for item enchanting -> May be possible via stoping block interaction with enchantment table
//TODO Support for golem construction



public class EventListener implements Listener {

	private final PermitMe manager;

	
	public EventListener( PermitMe manager ) {
		this.manager = manager;
	}

	
	@EventHandler
	public void onBlockBreak( BlockBreakEvent event ) {
		PermitMe.log.info("onBlockBreak event");
		// get basic information
		Block block = event.getBlock();
		int id = block.getTypeId();
		int data = block.getData();
		Player player = event.getPlayer();
		// check if the player is exempt
		if ( manager.playerManager.hasPermission( player, "PermitMe.exempt")) return;
		// get a list of what permits we may need to check for
		PermitManager pm = manager.permitManager;
		Set< String > permitsToCheck = pm.getCombinedPermits( id, data, 
				pm.blockBreakingIndex, pm.blockBreakingComplexIndex );
		if (( permitsToCheck != null ) & ( permitsToCheck.size() > 0 )) {
			// see if the player has any of the listed permits
			if ( manager.playerManager.hasPermit( player, permitsToCheck )) return;
			// failed check - block the event
			event.setCancelled(true);
			player.sendMessage("You don't have the correct permit to break that");
		}	
	}
	
	
	@EventHandler
	public void onBlockPlace( BlockPlaceEvent event ) {
		// TODO: See how BlockCanBuildEvent is different from BlockPlaceEvent
		// TODO: Extend to golem construction checking
		// get basic information
		Block block = event.getBlock();
		int id = block.getTypeId();
		int data = block.getData();
		Player player = event.getPlayer();
		// check if the player is exempt
		if ( manager.playerManager.hasPermission( player, "PermitMe.exempt")) return;
		// get a list of what permits we may need to check for
		PermitManager pm = manager.permitManager;
		Set< String > permitsToCheck = pm.getCombinedPermits( id, data, 
					pm.blockPlacingIndex, pm.blockPlacingComplexIndex );
		if (( permitsToCheck != null ) & ( permitsToCheck.size() > 0 )) {
			// see if the player has any of the listed permits
			if ( manager.playerManager.hasPermit( player, permitsToCheck )) return;
			// failed check - block the event
			event.setCancelled(true);
			player.sendMessage("You don't have the correct permit to place that");
		}	
	}
	
	
	@EventHandler
	public void onPlayerInteract( PlayerInteractEvent event ) {
		// TODO: Debug
		PermitMe.log.info("onPlayerInteract event");
		// Handles player left and right clicks
		// get basic info
		Player player = event.getPlayer();
		//Action action = event.getAction();
		Block block = event.getClickedBlock();
		ItemStack item = player.getItemInHand();
		int data = item.getData().getData();
		int id = item.getTypeId();
		//Material mat;
		//if ( block == null ) mat = Material.AIR; 
		//else mat = block.getType();
		// check item use restrictions
		PermitManager pm = manager.permitManager;
		Set< String > permitsToCheck = pm.getCombinedPermits(id, data,
				pm.itemUseIndex, pm.itemUseComplexIndex );
		if (( permitsToCheck != null ) & ( permitsToCheck.size() > 0 )) {
			if ( manager.playerManager.hasPermit(player, permitsToCheck) == false ) {
				// failed - block the item use
				event.setCancelled(true);
				player.sendMessage("You don't have the correct permit to use that");
				for ( String permit : permitsToCheck ) {
					player.sendMessage("You'll need a " + permit );
				}
				return;
			}
		}
		// check block use restrictions
		permitsToCheck = pm.getCombinedPermits( block.getTypeId(), block.getData(),
				pm.blockUseIndex, pm.blockUseComplexIndex );
		if (( permitsToCheck != null ) & ( permitsToCheck.size() > 0 )) {
			if ( manager.playerManager.hasPermit( player, permitsToCheck ) == false ) {
				// failed - can't click while pointing at this block
				event.setCancelled(true);
				player.sendMessage("You don't have the correct permit to activate that block");
				return;				
			}
		}
		PermitMe.log.info("onPlayerInteract approved");
	}
	
	
	@EventHandler
	public void onCraft( CraftItemEvent event ) {
		ItemStack craftingResult = event.getRecipe().getResult();
		int id = craftingResult.getTypeId();
		int data = craftingResult.getDurability();
		Player player = (Player)event.getWhoClicked();
		PermitManager pm = manager.permitManager;
		PermitMe.log.info("[PermitMe] DEBUG: Crafting event with ID " + id + " data " + data );
		Set< String > permitsToCheck = pm.getCombinedPermits( id, data,
				pm.itemCraftingIndex, pm.itemCraftingComplexIndex );
		if (( permitsToCheck != null ) & ( permitsToCheck.size() > 0 )) {
			if ( manager.playerManager.hasPermit( player, permitsToCheck ) == false ) {
				event.setCancelled( true );
				player.sendMessage("You don't have the correct permit to craft that");
				return;
			}
		}
		PermitMe.log.info("Crafting event approved");
	}
	
	@EventHandler
	public void onCraftingPrepared( PrepareItemCraftEvent event ) {
		ItemStack craftingResult = event.getRecipe().getResult();
		int id = craftingResult.getTypeId();
		int data = craftingResult.getDurability();
		List< HumanEntity > viewers = event.getViewers();
		PermitManager pm = manager.permitManager;
		PermitMe.log.info("[PermitMe] DEBUG: CraftingPrepared event with ID " + id + " data " + data );
		boolean ableToCraft = false;
		Set< String > permitsToCheck = pm.getCombinedPermits(id, data, 
				pm.itemCraftingIndex, pm.itemCraftingComplexIndex );
		if (( permitsToCheck != null ) & ( permitsToCheck.size() > 0 )) {
			for ( HumanEntity viewer : viewers ) {
				if ( manager.playerManager.hasPermit( (Player)viewer, permitsToCheck )) {
					ableToCraft = true;
					break;
				}
			}
			if ( ableToCraft == false ) {
				for ( HumanEntity viewer : viewers ) {
					Player p = (Player)viewer;
					p.sendMessage("Your crafting went horriably wrong");
				}
				// get the location, so we can spit items back out
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
					PermitMe.log.warning( "[PermitMe] !! Unable to find cast type for item " + holder.toString() + " (" + holder.getClass().toString() + ")");
				}
				// find which blocks (in a 3x3 grid) are empty, and therefore can have items 'thrown' at them
				Block center = location.getBlock();
				List< Location > emptySpaces = new LinkedList< Location >();
				for ( int x = -1; x < 2; x++ )
					for ( int y = -1; y < 2; y++ )
						for ( int z = -1; z < 2; z++ ) {
							Block neighbour = center.getRelative( x, y, z);
							if ( neighbour.isEmpty() | neighbour.isLiquid())
								emptySpaces.add( neighbour.getLocation());
						}
				if ( emptySpaces.size() == 0 ) 
					emptySpaces.add( location.add(0, 1, 0));
				// scatter items 
				boolean removedResult = false;
				CraftingInventory inventory = event.getInventory();
				for ( ItemStack item : inventory ) {
					if ( item != null ) {
						// see if this is from the "result" slot of the crafting grid
						if ((removedResult == false ) & ( item.getTypeId() == id ) & ( item.getDurability() == data )) {
							// match, so just remove the item
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
			}
		}
	}
	

	
}
