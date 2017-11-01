package com.gmail.redstonebunny.rsdb;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.bukkit.selections.CuboidSelection;
import com.sk89q.worldedit.bukkit.selections.Selection;

public class WorldEditHelper
{
	public enum Direction {
		SOUTH, WEST, NORTH, EAST
	}
	
	// NORTH = -z
	// SOUTH = +z
	// EAST = +x
	// WEST = -x
	// UP = +y
	// DOWN = -y
	public enum SelectionDirection {
		X, Y, Z, INVALID
	}
	
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
	
	public static boolean isFacingUp(Player player) {
		return player.getLocation().getPitch() < 0;
	}
	
	public static boolean isWorldEditInstalled()
	{
		return getWE() != null;
	}

	public static boolean isWorldEditEnabled()
	{
		return Bukkit.getServer().getPluginManager().isPluginEnabled("WorldEdit");
	}

	private static WorldEditPlugin getWE()
	{
		return (WorldEditPlugin) Bukkit.getServer().getPluginManager().getPlugin("WorldEdit");
	}

	public static boolean hasSelection(Player player)
	{
		return getSelection(player) != null;
	}

	public static boolean isCorrectWorld(Player player)
	{
		return getWE().getSelection(player).getWorld().equals(player.getWorld());
	}

	public static boolean isCubicSelection(Player player)
	{
		return getSelection(player) instanceof CuboidSelection;
	}

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
