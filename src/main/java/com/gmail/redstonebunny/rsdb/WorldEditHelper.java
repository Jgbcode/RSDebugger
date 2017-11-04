package com.gmail.redstonebunny.rsdb;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.bukkit.selections.CuboidSelection;
import com.sk89q.worldedit.bukkit.selections.Selection;

/*
 * 	WorldEditHelper class - contains many static methods relating to worldedit
 */

public class WorldEditHelper
{
	// Enum for players look direction
	public enum Direction {
		SOUTH, WEST, NORTH, EAST
	}
	
	// NORTH = -z
	// SOUTH = +z
	// EAST = +x
	// WEST = -x
	// UP = +y
	// DOWN = -y
	// Enum for one dimensional selection directions
	public enum SelectionDirection {
		X, Y, Z, INVALID
	}
	
	/*
	 * 	Parameters:
	 * 		Selection s - the players selection
	 * 
	 * 	Returns:
	 * 		SelectionDirection indicating the direction of the selection. 
	 * 		If the selection is not one dimensional SelectionDirection.INVALID is returned.
	 */
	public static SelectionDirection getSelectionDirection(Selection s) {
		int w = s.getWidth();
		int l = s.getLength();
		int h = s.getHeight();
		
		if(w == 1 && l == 1)
			return SelectionDirection.Y;
		if(w == 1 && h == 1)
			return SelectionDirection.Z;
		if(l == 1 && h == 1)
			return SelectionDirection.X;
		
		return SelectionDirection.INVALID;
	}
	
	/*
	 * 	Parameters:
	 * 		Player player - the player whose direction is to be returned
	 * 
	 * 	Returns:
	 * 		Direction indicating the direction the player is facing. 
	 * 		Returns invalid direction if the player is directly inbetween two directions.
	 */
	public static Direction getPlayerDirection(Player player) {
		float direction = (player.getLocation().getYaw() % 360) < 0 ? player.getLocation().getYaw() + 360 : player.getLocation().getYaw();

		if (315 <= direction || direction < 45)
		{
			return Direction.SOUTH;
		}
		else if (45 <= direction && direction < 135)
		{
			return Direction.WEST;
		}
		else if (135 <= direction && direction < 225)
		{
			return Direction.NORTH;
		}
		else if (225 <= direction && direction < 315)
		{
			return Direction.EAST;
		}
		else
		{
			player.sendMessage(RSDB.prefix + "Invalid direction. Please use \"/rsbd help\" for more information.");
			return null;
		}
	}
	
	/*
	 * 	Parameters:
	 * 		Player player - the player to be tested if they are facing up
	 * 
	 * 	Returns:
	 * 		boolean that is true if the player is looking up and false if they are looking down
	 */
	public static boolean isFacingUp(Player player) {
		return player.getLocation().getPitch() < 0;
	}
	
	/*
	 * 	Returns:
	 * 		True if worldedit is installed
	 */
	public static boolean isWorldEditInstalled()
	{
		return getWE() != null;
	}

	/*
	 * 	Returns:
	 * 		True if worldedit is enabled
	 */
	public static boolean isWorldEditEnabled()
	{
		return Bukkit.getServer().getPluginManager().isPluginEnabled("WorldEdit");
	}

	/*
	 * 	Returns:
	 * 		WorldEditPlugin instance
	 */
	private static WorldEditPlugin getWE()
	{
		return (WorldEditPlugin) Bukkit.getServer().getPluginManager().getPlugin("WorldEdit");
	}

	/*
	 * 	Parameters:
	 * 		Player player - the player to check
	 * 
	 * 	Returns:
	 * 		True if the player has made a selection
	 */
	public static boolean hasSelection(Player player)
	{
		return getSelection(player) != null;
	}

	/*
	 * 	Parameters:
	 * 		Player player - the player to check
	 * 
	 * 	Returns:
	 * 		True if the player is in the same world has his selection
	 */
	public static boolean isCorrectWorld(Player player)
	{
		return getWE().getSelection(player).getWorld().equals(player.getWorld());
	}

	/*
	 * 	Parameters:
	 * 		Player player - the player to check
	 * 
	 * 	Returns:
	 * 		True if the player's selection is a cuboid
	 */
	public static boolean isCubicSelection(Player player)
	{
		return getSelection(player) instanceof CuboidSelection;
	}

	/*
	 * 	Parameters:
	 * 		Player player - the player to check
	 * 
	 * 	Returns:
	 * 		The players selection
	 */
	public static Selection getSelection(Player player)
	{
		if (!isWorldEditEnabled())
		{
			player.sendMessage(RSDB.prefix + "Could not access WorldEdit, please check if it's installed and enabled.");
			return null;
		}

		Selection sel = ((WorldEditPlugin) Bukkit.getServer().getPluginManager().getPlugin("WorldEdit")).getSelection(player);

		if (sel == null)
		{
			player.sendMessage(RSDB.prefix + "Please make a WorldEdit selection before using this command.");
			return null;
		}
		if (!sel.getWorld().equals(player.getWorld()))
		{
			player.sendMessage(RSDB.prefix + "You should be in the same world as your WorldEdit selection.");
			return null;
		}
		if (!(sel instanceof CuboidSelection))
		{
			player.sendMessage(RSDB.prefix + "This command only accepts cuboid selection, use \"//sel ?\" if you don't know how to change your selection type.");
			return null;
		}
		return sel;
	}
}
