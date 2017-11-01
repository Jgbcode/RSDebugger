package com.gmail.redstonebunny.rsdb.script;

import java.util.ArrayList;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.material.Redstone;
import org.bukkit.material.RedstoneWire;

import com.gmail.redstonebunny.rsdb.RSDB;
import com.gmail.redstonebunny.rsdb.WorldEditHelper;
import com.sk89q.worldedit.bukkit.selections.Selection;

public class Watchpoint {
	private String name;
	private int value;
	private ArrayList<Location> sensors;
	private String prefix;
	private String varPrefix;
	
	
	private enum SelectionStatus {
		POSITIVE, NEGATIVE, FAILURE
	}
	
	/*
	 * 		Parameters:
	 * 			String name - the name of the watchpoint to be created
	 * 			Player p - the player who is creating the watchpoint
	 * 
	 * 		Return Value:
	 * 			WatchPoint - the created watchpoint, null if error occured
	 */
	public static Watchpoint createWatchpoint(String name, Player p) {
		Selection s = WorldEditHelper.getSelection(p);
		
		if(name.startsWith("$") || name.startsWith("#")) {
			p.sendMessage(RSDB.prefix + "Failed to create watchpoint: Illegal watchpoint name.");
			return null;
		}
		
		SelectionStatus status = validateSelection(s, p);
		if(status == SelectionStatus.FAILURE) {
			return null;
		}
		boolean neg = status == SelectionStatus.NEGATIVE;
		
		Watchpoint w = new Watchpoint(name, s, neg);
		
		if(w.getNumberOfBits() == 0) {
			p.sendMessage(RSDB.prefix + "Failed to create watchpoint: The selection does not contain any redstone components.");
			return null;
		} else {
			p.sendMessage(RSDB.successPrefix + "Successfully created watchpoint \"" + name + "\" :");
			w.printBitLocations(p);
		}
		
		return w;
	}
	
	public void appendSensors(Player p) {
		Selection s = WorldEditHelper.getSelection(p);
		
		SelectionStatus status = validateSelection(s, p);
		if(status == SelectionStatus.FAILURE) {
			return;
		}
		boolean neg = status == SelectionStatus.NEGATIVE;
		
		int prevSize = sensors.size();
		
		addSensors(s, neg);
		
		if(prevSize == sensors.size()) {
			p.sendMessage(RSDB.prefix + prefix + "Failed to append bits: The selection does not contain any redstone components.");
		} else {
			p.sendMessage(RSDB.prefix + prefix + ChatColor.GREEN + "Successfully added " + (sensors.size() - prevSize) + 
					" bits to watchpoint. " + sensors.size() + " total bits in watchpoint.");
			for(int i = prevSize; i < sensors.size(); i++) {
				p.sendMessage(RSDB.prefix + prefix + "bit" + i + ": block=" + sensors.get(i).getBlock().getType().toString() + 
						" x=" + sensors.get(i).getBlockX() + " y=" + sensors.get(i).getBlockY() + " z=" + sensors.get(i).getBlockZ());
			}
		}
	}
	
	/*
	 * 		Parameters:
	 * 			String name - the name of the watchpoint
	 * 			Selection s - the worldedit selection to evaluate
	 * 			boolean neg - indicates the position of the MSB in the selection, if true the MSB is the most negative position
	 */
	public Watchpoint(String name, Selection s, boolean neg) {
		this.name = name;
		this.value = 0;
		this.prefix = ChatColor.BLACK + "[" + ChatColor.DARK_GREEN + name + ChatColor.BLACK + "] " + ChatColor.GRAY;
		this.varPrefix = ChatColor.BLACK + "[" + ChatColor.GOLD + "$" + ChatColor.DARK_GREEN + name + ChatColor.BLACK + "] " + ChatColor.GRAY;
		this.sensors = new ArrayList<Location>();
		
		addSensors(s, neg);
	}
	
	/*
	 * 		Description:
	 * 			Returns the number of bits this watchpoint is observing
	 */
	public int getNumberOfBits() {
		return sensors.size();
	}
	
	/*
	 * 		Description:
	 * 			Prints each bit's location to the player
	 */
	public void printBitLocations(Player p) {
		for(int i = 0; i < sensors.size(); i++) {
			p.sendMessage(RSDB.prefix + prefix + "bit" + i + ": block=" + sensors.get(i).getBlock().getType().toString() + 
					" x=" + sensors.get(i).getBlockX() + " y=" + sensors.get(i).getBlockY() + " z=" + sensors.get(i).getBlockZ());
		}
	}
	
	/*
	 * 		Return Type:
	 * 			Returns false if any of the sensors are missing
	 * 
	 * 		Description:
	 * 			Recalculates this watchpoint's using the most recent data
	 */
	public boolean updateValue() {
		value = 0;
		boolean ret = true;
		for(int i = 0; i < sensors.size(); i++) {
			if(sensors.get(i).getBlock().getState().getData() instanceof Redstone) {
				if(((RedstoneWire)(sensors.get(i).getBlock().getState().getData())).isPowered()) {
					value += 1 << i;
				}
			} else {
				ret = false;
			}
		}
		
		return ret;
	}
	
	/*
	 * 		Description:
	 * 			Prints the current value of this watchpoint to the player
	 */
	public void printValue(Player p) {
		if(!updateValue()) {
			p.sendMessage(RSDB.errorPrefix + "The watchpoint \"" + name + "\" has an invalid sensor. Did you break some redstone? Please reselect the watchpoint with"
					+ " the command \"/rsdb watch " + name + "\".");
		}
		String bin = "";
		for(int i = 0; i < sensors.size(); i++) {
			bin = (((value & (1 << i)) == 0) ? "0" : "1") + bin;
		}
		p.sendMessage(RSDB.prefix + varPrefix + value + " = " + bin + "b");
	}
	
	/*
	 * 		Description:
	 * 			Adds a sensor to the watchpoint
	 */
	private void addSensors(Selection s, boolean neg) {
		Location min = s.getMinimumPoint();
		Location max = s.getMaximumPoint();
		Location inc = new Location(max.getWorld(), max.getBlockX(), max.getBlockY(), max.getBlockZ());
		inc.subtract(min);
		inc.multiply(1 / min.distance(max));
		
		if(neg) {
			min.subtract(inc);
			for(Location l = max.clone(); !(l.getBlockX() == min.getBlockX() && l.getBlockY() == min.getBlockY() && l.getBlockZ() == min.getBlockZ()); l.subtract(inc)) {
				if(l.getBlock().getState().getData() instanceof Redstone) {
					sensors.add(l.clone());
				}
			}
		} else {
			max.add(inc);
			for(Location l = min.clone(); !(l.getBlockX() == max.getBlockX() && l.getBlockY() == max.getBlockY() && l.getBlockZ() == max.getBlockZ()); l.add(inc)) {
				if(l.getBlock().getState().getData() instanceof Redstone) {
					sensors.add(l.clone());
				}
			}
		}
	}
	
	/*
	 * 		Description:
	 * 			Validates the players selection.
	 */
	private static SelectionStatus validateSelection(Selection s, Player p) {
		if(s != null) {
			WorldEditHelper.SelectionDirection sd = WorldEditHelper.getSelectionDirection(s);
			if(sd == WorldEditHelper.SelectionDirection.INVALID) {
				p.sendMessage(RSDB.prefix + "Selection failed: Watchpoint selections must be one-dimensional.");
				return SelectionStatus.FAILURE;
			}
			
			WorldEditHelper.Direction dir = WorldEditHelper.getPlayerDirection(p);
			boolean isUp = WorldEditHelper.isFacingUp(p);
			
			if(sd == WorldEditHelper.SelectionDirection.X) {
				if(dir == WorldEditHelper.Direction.NORTH || dir == WorldEditHelper.Direction.SOUTH) {
					p.sendMessage(RSDB.prefix + "Invalid direction: You must face in a direction parallel to your watchpoint selection.");
					return SelectionStatus.FAILURE;
				}
				return (dir == WorldEditHelper.Direction.WEST) ? SelectionStatus.NEGATIVE : SelectionStatus.POSITIVE;
			} else if(sd == WorldEditHelper.SelectionDirection.Y) {
				return !isUp ? SelectionStatus.NEGATIVE : SelectionStatus.POSITIVE;
			} else {
				if(dir == WorldEditHelper.Direction.EAST || dir == WorldEditHelper.Direction.WEST) {
					p.sendMessage(RSDB.prefix + "Invalid direction: You must face in a direction parallel to your watchpoint selection.");
					return SelectionStatus.FAILURE;
				}
				return (dir == WorldEditHelper.Direction.NORTH) ? SelectionStatus.NEGATIVE : SelectionStatus.POSITIVE;
			}
		} else {
			return SelectionStatus.FAILURE;
		}
	}
}
