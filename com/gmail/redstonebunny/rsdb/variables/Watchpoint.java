package com.gmail.redstonebunny.rsdb.variables;

import java.util.ArrayList;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.material.Redstone;

import com.gmail.redstonebunny.rsdb.RSDB;
import com.gmail.redstonebunny.rsdb.WorldEditHelper;
import com.sk89q.worldedit.bukkit.selections.Selection;

public class Watchpoint extends Variable{
	private String prefix;
	private Player p;
	
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
	public static Watchpoint createWatchpoint(String name, Player p, Variable size) {
		Selection s = WorldEditHelper.getSelection(p);
		
		if(!Variable.isLegalVarName(name)) {
			p.sendMessage(RSDB.prefix + "Failed to create watchpoint: Watchpoint name contains illegal characters.");
			return null;
		}
		
		SelectionStatus status = validateSelection(s, p);
		if(status == SelectionStatus.FAILURE) {
			return null;
		}
		boolean neg = status == SelectionStatus.NEGATIVE;
		
		Watchpoint w = new Watchpoint(name, s, neg, size, p);
		
		if(w.getNumberOfBits() == 0) {
			p.sendMessage(RSDB.prefix + "Failed to create watchpoint: The selection does not contain any redstone components.");
			return null;
		} else {
			p.sendMessage(RSDB.successPrefix + "Successfully created watchpoint \"" + name + "\" :");
			w.printLocation(p);
		}
		
		return w;
	}
	
	/*
	 * 		Parameters:
	 * 			String name - the name of the watchpoint
	 * 			Selection s - the worldedit selection to evaluate
	 * 			boolean neg - indicates the position of the MSB in the selection, if true the MSB is the most negative position
	 */
	public Watchpoint(String name, Selection s, boolean neg, Variable size, Player p) {
		super(name, new ArrayList<Location>(), size);
		this.prefix = ChatColor.BLACK + "[" + ChatColor.DARK_GREEN + name + ChatColor.BLACK + "] " + ChatColor.GRAY;
		this.p = p;
		
		addSensors(s, neg);
	}
	
	public void appendSensors(Player p) {
		Selection s = WorldEditHelper.getSelection(p);
		
		SelectionStatus status = validateSelection(s, p);
		if(status == SelectionStatus.FAILURE) {
			return;
		}
		boolean neg = status == SelectionStatus.NEGATIVE;
		
		int prevSize = l.size();
		
		addSensors(s, neg);
		
		if(prevSize == l.size()) {
			p.sendMessage(RSDB.prefix + prefix + "Failed to append bits: The selection does not contain any redstone components.");
		} else {
			p.sendMessage(RSDB.prefix + prefix + ChatColor.GREEN + "Successfully added " + (l.size() - prevSize) + 
					" bits to watchpoint. " + l.size() + " total bits in watchpoint.");
			for(int i = prevSize; i < l.size(); i++) {
				p.sendMessage(RSDB.prefix + prefix + "bit" + i + ": block=" + l.get(i).getBlock().getType().toString() + 
						" x=" + l.get(i).getBlockX() + " y=" + l.get(i).getBlockY() + " z=" + l.get(i).getBlockZ());
			}
		}
	}
	
	/*
	 * 		Return Type:
	 * 			Returns false if any of the sensors are missing
	 * 
	 * 		Description:
	 * 			Recalculates this watchpoint's using the most recent data
	 */
	public int currentValue() {
		int tmp = 0;
		boolean ret = true;
		for(int i = 0; i < l.size(); i++) {
			if(l.get(i).getBlock().getState().getData() instanceof Redstone) {
				if(((Redstone)(l.get(i).getBlock().getState().getData())).isPowered()) {
					tmp += 1 << i;
				}
			} else {
				ret = false;
			}
		}
		
		if(!ret) {
			p.sendMessage(RSDB.errorPrefix + "The watchpoint \"" + name + "\" has an invalid sensor. Did you break some redstone? Please reselect the watchpoint with"
					+ " the command \"/rsdb watch " + name + "\".");
		}
		return (ret) ? tmp : -1;
	}
	
	/*
	 * 		Description:
	 * 			Adds a sensor to the watchpoint
	 */
	private void addSensors(Selection s, boolean neg) {
		Location min = s.getMinimumPoint();
		Location max = s.getMaximumPoint();
		
		if(min.getX() == Double.NaN)
			min = max.clone();
		
		if(max.getX() == Double.NaN)
			max = min.clone();
		
		Location inc = new Location(max.getWorld(), max.getBlockX(), max.getBlockY(), max.getBlockZ());
		inc.subtract(min);
		inc.multiply(1 / min.distance(max));
		
		if(neg) {
			min.subtract(inc);
			System.out.println(max);
			System.out.println(min);
			for(Location l = max.clone(); !l.equals(max); l.subtract(inc)) {
				if(l.getBlock().getState().getData() instanceof Redstone) {
					System.out.println(l);
					super.l.add(l.clone());
				}
			}
		} else {
			max.add(inc);
			System.out.println(max);
			System.out.println(min);
			for(Location l = min.clone(); !l.equals(max); l.add(inc)) {
				if(l.getBlock().getState().getData() instanceof Redstone) {
					System.out.println(l);
					super.l.add(l.clone());
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

	@Override
	protected void updateChildren() {
		super.setValue(currentValue());
	}
	
	@Override
	public void setValue(int value) {
		p.sendMessage(RSDB.prefix + prefix + "You cannot set the value of watchpoints.");
	}
	
	@Override
	public Integer getValue() {
		currentValue = currentValue();
		return super.getValue();
	}
}
