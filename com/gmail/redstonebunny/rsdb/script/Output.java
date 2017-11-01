package com.gmail.redstonebunny.rsdb.script;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import com.gmail.redstonebunny.rsdb.RSDB;

public abstract class Output {
	protected Location l;
	protected RSDB rsdb;
	protected Player p;
	protected final String prefix;
	protected final String successPrefix;
	protected final String errorPrefix;
	protected static final Vector[] surround = {new Vector(0, 0, 1), new Vector(0, 0, -1), new Vector(0, -1, 0), new Vector(1, 0, 0), new Vector(-1, 0, 0)};
	
	protected Output(Location l, RSDB rsdb, Player p, String prefix, String successPrefix, String errorPrefix) {
		this.l = l;
		this.rsdb = rsdb;
		this.p = p;
		this.prefix = prefix;
		this.successPrefix = successPrefix;
		this.errorPrefix = errorPrefix;
	}
	
	public boolean pulse(int numTicks, final Material replace) {
		if(!validatePulse())
			return false;
		
		l.getBlock().setType(Material.REDSTONE_BLOCK);
		new BukkitRunnable() {
			@Override
			public void run() {
				l.getBlock().setType(replace);
			}
		}.runTaskLater(rsdb, numTicks);
		
		return true;
	}
	
	public boolean toggle(final Material replace) {
		if(l.getBlock().getType().equals(replace)) {
			l.getBlock().setType(Material.REDSTONE_BLOCK);
			return true;
		} else if(l.getBlock().getType().equals(Material.REDSTONE_BLOCK)) {
			l.getBlock().setType(replace);
			return true;
		}
		
		return false;
	}
	
	public void print() {
		p.sendMessage(RSDB.prefix + prefix + "x=" + l.getBlockX() + " y=" + l.getBlockY() + " z=" + l.getBlockZ());
	}
	
	protected abstract boolean validatePulse();
}
