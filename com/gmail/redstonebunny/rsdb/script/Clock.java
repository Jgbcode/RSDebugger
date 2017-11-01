package com.gmail.redstonebunny.rsdb.script;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.material.RedstoneWire;

import com.gmail.redstonebunny.rsdb.RSDB;
import com.gmail.redstonebunny.rsdb.WorldEditHelper;
import com.sk89q.worldedit.bukkit.selections.Selection;

public class Clock extends Output{
	private int count;
	private static final String prefix = ChatColor.DARK_BLUE + "[" + ChatColor.AQUA + "CLK" + ChatColor.DARK_BLUE + "] " + ChatColor.GRAY;
	private static final String successPrefix = prefix + ChatColor.GREEN;
	private static final String errorPrefix = prefix + ChatColor.RED;
	
	public static Clock createClock(RSDB rsdb, Player p) {
		Selection s = WorldEditHelper.getSelection(p);
		
		Location l;
		if(s.getWidth() == 1 && s.getLength() == 1 && s.getHeight() == 1) {
			l = s.getMinimumPoint();
		} else {
			p.sendMessage(RSDB.prefix + prefix + "Unable to create clock: The clock selection must be a single block.");
			return null;
		}
		
		boolean foundClock = false;
		for(int i = 0; i < surround.length; i++) {
			Location tmp = l.clone().add(surround[i]);
			if(tmp.getBlock().getState().getData() instanceof RedstoneWire) {
				foundClock = true;
				break;
			}
		}
		
		if(!foundClock) {
			p.sendMessage(RSDB.prefix + prefix + "Unable to create clock: The selected block is not connected to a redstone wire.");
			return null;
		}
		
		if(l.getBlock().getType().equals(Material.GLASS)) {
			Clock c = new Clock(l, rsdb, p);
			p.sendMessage(RSDB.prefix + successPrefix + "Successfully created a clock:");
			c.print();
			return c;
		} else {
			p.sendMessage(RSDB.prefix + prefix + "Unable to create clock: The clock selection must be a block of glass.");
			return null;
		}
	}
	
	private Clock(Location l, RSDB rsdb, Player p) {
		super(l, rsdb, p, prefix, successPrefix, errorPrefix);
		this.count = 0;
	}
	
	@Override
	protected boolean validatePulse() {
		if(!l.getBlock().getType().equals(Material.GLASS)) {
			p.sendMessage(RSDB.prefix + errorPrefix + "Unable to trigger clock: The clock source was modified.");
			return false;
		}
		
		for(int i = 0; i < surround.length; i++) {
			Location tmp = l.clone().add(surround[i]);
			if(tmp.getBlock().getState().getData() instanceof RedstoneWire && ((RedstoneWire)tmp.getBlock().getState().getData()).isPowered()) {
				p.sendMessage(RSDB.prefix + errorPrefix + "Unable to trigger clock: Clock line is already powered");
				return false;
			}
		}
		
		count++;
		return true;
	}
	
	public boolean pulse(int numTicks) {
		return super.pulse(numTicks, Material.GLASS);
	}
	
	public boolean toggle() {
		if(l.getBlock().getType().equals(Material.GLASS)) {
			count++;
		}
		if(!super.toggle(Material.GLASS)) {
			p.sendMessage(RSDB.prefix + errorPrefix + "Unable to trigger clock: The clock source was modified.");
			return false;
		}
		
		return true;
	}
	
	public void printCount(Player p) {
		p.sendMessage(RSDB.prefix + prefix + count + " pulse(s)");
	}
	
	public int getCount() {
		return count;
	}
	
	public void resetCount() {
		count = 0;
	}
}
