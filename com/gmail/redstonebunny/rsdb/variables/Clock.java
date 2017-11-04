package com.gmail.redstonebunny.rsdb.variables;

import java.util.HashMap;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.material.RedstoneWire;

import com.gmail.redstonebunny.rsdb.RSDB;
import com.gmail.redstonebunny.rsdb.WorldEditHelper;
import com.sk89q.worldedit.bukkit.selections.Selection;

public class Clock extends Output{
	private HashMap<String, Variable> vars;
	
	public static Clock createClock(RSDB rsdb, Player p, Variable size, HashMap<String, Variable> vars) {
		Selection s = WorldEditHelper.getSelection(p);
		
		if(s == null)
			return null;
		
		Location l;
		if(s.getWidth() == 1 && s.getLength() == 1 && s.getHeight() == 1) {
			l = s.getMinimumPoint();
		} else {
			p.sendMessage(RSDB.prefix + "Unable to create clock: The clock selection must be a single block.");
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
			p.sendMessage(RSDB.prefix + "Unable to create clock: The selected block is not connected to a redstone wire.");
			return null;
		}
		
		if(l.getBlock().getType().equals(Material.GLASS)) {
			Clock c = new Clock(l, rsdb, p, size, vars);
			p.sendMessage(RSDB.successPrefix + "Successfully created a clock:");
			c.printLocation(p);
			return c;
		} else {
			p.sendMessage(RSDB.prefix + "Unable to create clock: The clock selection must be a block of glass.");
			return null;
		}
	}
	
	private Clock(Location l, RSDB rsdb, Player p, Variable size, HashMap<String, Variable> vars) {
		super(l, rsdb, p, "#CLOCK", size);
		this.vars = vars;
	}
	
	public boolean pulse(int numTicks) {
		for(Variable v : vars.values()) {
			if(v != null)
				v.update();
		}
		return super.pulse(numTicks, Material.GLASS);
	}
	
	public boolean toggle() {
		if(super.l.get(0).getBlock().getType().equals(Material.GLASS)) {
			for(Variable v : vars.values()) {
				if(v != null)
					v.update();
			}
		}
		
		if(!super.toggle(Material.GLASS)) {
			p.sendMessage(RSDB.errorPrefix + "Unable to trigger clock: The clock source was modified.");
			return false;
		}
		
		return true;
	}
	
	@Override
	protected boolean validatePulse() {
		if(!l.get(0).getBlock().getType().equals(Material.GLASS)) {
			p.sendMessage(RSDB.errorPrefix + "Unable to trigger clock: The clock source was modified.");
			return false;
		}
		
		for(int i = 0; i < surround.length; i++) {
			Location tmp = l.get(0).clone().add(surround[i]);
			if(tmp.getBlock().getState().getData() instanceof RedstoneWire && ((RedstoneWire)tmp.getBlock().getState().getData()).isPowered()) {
				p.sendMessage(RSDB.errorPrefix + "Unable to trigger clock: Clock line is already powered");
				return false;
			}
		}
		
		return true;
	}
}
