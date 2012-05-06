package com.oreilly.permitme;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

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

import com.oreilly.permitme.data.ReverseComplexPermitRecord;
import com.oreilly.permitme.data.ReversePermitRecord;
import com.oreilly.permitme.permit.PermitManager;


//TODO Support for item enchanting -> May be possible via stoping block interaction with enchantment table
//TODO Support for golem construction



public class EventListener implements Listener {

	private final PermitMe manager;
	
	public static final Integer[] SIGN_ID_LIST = { 63, 68 };

	
	public EventListener( PermitMe manager ) {
		this.manager = manager;
	}

	
	@EventHandler
	public void onBlockBreak( BlockBreakEvent event ) {
		// if the event has already been cancelled, don't do anything
		if ( event.isCancelled()) return;
		// if this player is exempt (via permissions), don't do anything
		Player player = event.getPlayer();
		if ( manager.playerManager.hasPermission( player, "exempt")) return;
		// check for permits, and cancel on fail
		Block block = event.getBlock();
		if ( hasPermit( player, block.getTypeId(), block.getData(), 
				manager.permitManager.blockBreakingIndex, 
				manager.permitManager.blockBreakingComplexIndex ) == false ) {
			player.sendMessage( "You don't have the correct permit to break that" );
			event.setCancelled( true );
		}
	}
	
	
	@EventHandler
	public void onBlockPlace( BlockPlaceEvent event ) {
		// TODO: See how BlockCanBuildEvent is different from BlockPlaceEvent
		// TODO: Extend to golem construction checking
		// TODO: Track sign placement, pass to new handler
		// if the event has already been cancelled, don't do anything
		if ( event.isCancelled()) return;
		// get basic information
		Block block = event.getBlock(); int id = block.getTypeId();
		int data = block.getData();	Player player = event.getPlayer();
		// check if the player is exempt
		if ( manager.playerManager.hasPermission( player, "exempt")) return;
		// check for correct permit
		if ( hasPermit( player, id, data, 
				manager.permitManager.blockPlacingIndex, 
				manager.permitManager.blockPlacingComplexIndex ) == false ) {
			player.sendMessage( "You don't have the correct permit to place that" );
			event.setCancelled( true );
		}
	}
	
	
	@EventHandler
	public void onPlayerInteract( PlayerInteractEvent event ) {
		// Handles left and right clicks
		// if the event has already been cancelled, don't do anything
		if ( event.isCancelled()) return;
		// get basic info
		Player player = event.getPlayer();
		Action action = event.getAction();
		Block block = event.getClickedBlock();
		ItemStack item = player.getItemInHand();
		// check for sign interaction
		for ( int signID : SIGN_ID_LIST ) {
			if ( block.getTypeId() == signID ) {
				if ( action == Action.LEFT_CLICK_BLOCK ) handleSignRefresh( event );
				if ( action == Action.RIGHT_CLICK_BLOCK ) handleSignInteraction( event );
				break;
			}
		}
		// check for exemption, and return if true
		if ( manager.playerManager.hasPermission( player, "PermitMe.exempt")) return;
		// check item use 
		if ( hasPermit( player, item.getTypeId(), item.getData().getData(), 
				manager.permitManager.itemUseIndex, 
				manager.permitManager.itemUseComplexIndex ) == false ) {
			player.sendMessage( "You don't have the correct permit to use that" );
			event.setCancelled( true );
		}		
		// check block use restrictions
		if ( action == Action.RIGHT_CLICK_BLOCK ) {
			if ( hasPermit( player, item.getTypeId(), item.getData().getData(), 
					manager.permitManager.blockUseIndex, 
					manager.permitManager.blockUseComplexIndex ) == false ) {
				player.sendMessage( "You don't have the correct permit to activate that block" );
				event.setCancelled( true );
			}
		}
	}


	@EventHandler
	public void onCraft( CraftItemEvent event ) {
		// if the event has already been cancelled, don't do anything
		if ( event.isCancelled()) return;
		// if the player is exempt, don't do anything
		Player player = (Player)event.getWhoClicked();
		if ( manager.playerManager.hasPermission( player, "exempt")) return;		
		// check crafting permission
		ItemStack craftingResult = event.getRecipe().getResult();
		if ( hasPermit( player, craftingResult.getTypeId(), craftingResult.getDurability(), 
				manager.permitManager.itemCraftingIndex, 
				manager.permitManager.itemCraftingComplexIndex ) == false ) {
			player.sendMessage( "You don't have the correct permit to craft that" );
			event.setCancelled( true );
		}		
	}
	

	@EventHandler
	public void onCraftingPrepared( PrepareItemCraftEvent event ) {
		// get data
		ItemStack craftingResult = event.getRecipe().getResult();
		int id = craftingResult.getTypeId();
		int data = craftingResult.getDurability();
		List< HumanEntity > viewers = event.getViewers();
		PermitManager pm = manager.permitManager;
		boolean ableToCraft = false;
		for ( HumanEntity viewer : viewers ) {
			if ( hasPermit( (Player)viewer, id, data, pm.itemCraftingIndex, pm.itemCraftingComplexIndex )) {
				ableToCraft = true;
				break;
			}
		}
		// only take action if no players (with the GUI open) are able to craft the recipie
		if ( ableToCraft == false ) {
			for ( HumanEntity viewer : viewers ) {
				Player p = (Player)viewer;
				p.sendMessage("This crafting went horriably wrong..");
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
			// close the open inventory GUI's to avoid graphical glitch
			for ( HumanEntity viewer : viewers ) {
				if ( Player.class.isInstance( viewer )) {
					Player player = (Player)viewer;
					player.closeInventory();
				}
			}				
		}
	}
	
	
	@EventHandler
	public void onSignChangeEvent( SignChangeEvent event ) {
		// TODO
		// Called when text on a sign is changed (or created)
		Player player = event.getPlayer();
		player.sendMessage("Text of sign has changed to " + StringUtils.join( event.getLines(), "|"));
	}

	
	private boolean hasPermit( Player player, int id, int data, 
			ReversePermitRecord simple, ReverseComplexPermitRecord complex ) {
		// returns true if event should be cancelled
		PermitManager pm = manager.permitManager;
		Set< String > permitsToCheck = pm.getCombinedPermits( id, data, 
				simple, complex );
		if (( permitsToCheck != null ) & ( permitsToCheck.size() > 0 )) {
			// see if the player has any of the listed permits
			if ( manager.playerManager.hasPermit( player, permitsToCheck )) return true;
			// failed check - return false
			return false;
		} else return true;
	}
	
	
	
	private void handleSignInteraction( PlayerInteractEvent event ) {
		// TODO
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
		// TODO
		Player player = event.getPlayer();
		player.sendMessage("Sign refresh tracked");
	}
	
	

}
