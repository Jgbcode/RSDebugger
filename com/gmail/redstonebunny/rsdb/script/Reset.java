package com.gmail.redstonebunny.rsdb.script;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.material.RedstoneWire;

import com.gmail.redstonebunny.rsdb.RSDB;
import com.gmail.redstonebunny.rsdb.WorldEditHelper;
import com.sk89q.worldedit.bukkit.selections.Selection;

public class Reset extends Output {
	private Player p;
	private static final String prefix = ChatColor.DARK_BLUE + "[" + ChatColor.AQUA + "RESET" + ChatColor.DARK_BLUE + "] " + ChatColor.GRAY;
	private static final String successPrefix = prefix + ChatColor.GREEN;
	private static final String errorPrefix = prefix + ChatColor.RED;
	
	public static Reset createReset(RSDB rsdb, Player p) {
		Selection s = WorldEditHelper.getSelection(p);
		
		Location l;
		if(s.getWidth() == 1 && s.getLength() == 1 && s.getHeight() == 1) {
			l = s.getMinimumPoint();
		} else {
			p.sendMessage(RSDB.prefix + prefix + "Unable to create reset: The reset selection must be a single block.");
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
			p.sendMessage(RSDB.prefix + prefix + "Unable to create reset: The selected block is not connected to a redstone wire.");
			return null;
		}
		
		if(l.getBlock().getType().equals(Material.GLASS)) {
			Reset r = new Reset(l, rsdb, p);
			p.sendMessage(RSDB.prefix + successPrefix + "Successfully created a reset:");
			r.print();
			return r;
		} else {
			p.sendMessage(RSDB.prefix + prefix + "Unable to create reset: The reset selection must be a block of glass.");
			return null;
		}
	}
	
	protected Reset(Location l, RSDB rsdb, Player p) {
		super(l, rsdb, p, prefix, successPrefix, errorPrefix);
		this.p = p;
	}

	@Override
	protected boolean validatePulse() {
		if(!l.getBlock().getType().equals(Material.GLASS)) {
			p.sendMessage(RSDB.prefix + errorPrefix + "Unable to trigger reset: The reset source was modified.");
			return false;
		}
		
		return true;
	}
	
	public boolean pulse(int numTicks) {
		return super.pulse(numTicks, Material.GLASS);
	}

}
