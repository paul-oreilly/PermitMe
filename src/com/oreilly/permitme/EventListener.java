package com.oreilly.permitme;

import java.util.Set;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import com.oreilly.permitme.permit.PermitManager;


// TODO: Block interaction limits
// TODO: Enchanting limits
// TODO: Golem limits
// TODO: Crafting limits


public class EventListener implements Listener {

	private final PermitMe manager;

	
	public EventListener( PermitMe manager ) {
		this.manager = manager;
	}

	
	@EventHandler
	public void onBlockBreak( BlockBreakEvent event ) {
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
				pm.breakingIndex, pm.breakingComplexIndex );
		if ( permitsToCheck != null ) {
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
					pm.placingIndex, pm.placingComplexIndex );
		if ( permitsToCheck != null ) {
			// see if the player has any of the listed permits
			if ( manager.playerManager.hasPermit( player, permitsToCheck )) return;
			// failed check - block the event
			event.setCancelled(true);
			player.sendMessage("You don't have the correct permit to place that");
		}	
	}
	
	
	@EventHandler
	public void onPlayerInteract( PlayerInteractEvent event ) {
		// Handles player left and right clicks
		// get basic info
		Player player = event.getPlayer();
		Action action = event.getAction();
		Block block = event.getClickedBlock();
		ItemStack item = player.getItemInHand();
		int data = item.getData().getData();
		int id = item.getTypeId();
		Material mat;
		if ( block == null ) mat = Material.AIR; 
		else mat = block.getType();
		// check item use restrictions
		PermitManager pm = manager.permitManager;
		Set< String > permitsToCheck = pm.getCombinedPermits(id, data,
				pm.usingIndex, pm.usingComplexIndex );
		if ( permitsToCheck != null ) {
			if ( manager.playerManager.hasPermit(player, permitsToCheck)) return;
			// failed - block the item use
			event.setCancelled(true);
			player.sendMessage("You don't have the correct permit to use that");
		}
	}
	
	
	
	
	
	// TODO: Further event listeners
	
}
