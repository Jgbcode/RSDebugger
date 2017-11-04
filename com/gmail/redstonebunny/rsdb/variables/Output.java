package com.gmail.redstonebunny.rsdb.variables;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import com.gmail.redstonebunny.rsdb.RSDB;

public abstract class Output extends Variable {
	protected RSDB rsdb;
	protected Player p;
	protected static final Vector[] surround = {new Vector(0, 0, 1), new Vector(0, 0, -1), new Vector(0, -1, 0), new Vector(1, 0, 0), new Vector(-1, 0, 0)};
	
	protected Output(Location l, RSDB rsdb, Player p, String name, Variable size) {
		super(name, l, size);
		this.rsdb = rsdb;
		this.p = p;
	}
	
	public boolean pulse(int numTicks, final Material replace) {
		if(!validatePulse())
			return false;
		
		currentValue++;
		l.get(0).getBlock().setType(Material.REDSTONE_BLOCK);
		new BukkitRunnable() {
			@Override
			public void run() {
				l.get(0).getBlock().setType(replace);
			}
		}.runTaskLater(rsdb, numTicks);
		
		return true;
	}
	
	public boolean toggle(final Material replace) {
		if(l.get(0).getBlock().getType().equals(replace)) {
			l.get(0).getBlock().setType(Material.REDSTONE_BLOCK);
			currentValue++;
			return true;
		} else if(l.get(0).getBlock().getType().equals(Material.REDSTONE_BLOCK)) {
			l.get(0).getBlock().setType(replace);
			return true;
		}
		
		return false;
	}
	
	public boolean getState() {
		return l.get(0).getBlock().getType().equals(Material.REDSTONE_BLOCK);
	}
	
	@Override
	public void setValue(int value) {
		p.sendMessage(RSDB.errorPrefix + "The value of " + "$" + name + " cannot be modified.");
	}
	
	@Override
	public void reset() {
		super.reset();
		currentValue = 0;
	}
	
	protected abstract boolean validatePulse();
}
