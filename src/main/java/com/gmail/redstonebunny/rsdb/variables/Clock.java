package com.gmail.redstonebunny.rsdb.variables;

import java.util.HashMap;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.material.RedstoneWire;

import com.gmail.redstonebunny.rsdb.RSDB;
import com.gmail.redstonebunny.rsdb.WorldEditHelper;
import com.sk89q.worldedit.bukkit.selections.Selection;

/*
 * 	Clock class - used for updating the circuit
 */

public class Clock extends Output{
	// List of all variables so they can be updated on the clock pulse
	private HashMap<String, Variable> vars;
	
	/*
	 * 	Parameters:
	 * 		RSDB rsdb - the main plugin instance
	 * 		Player p - the player who is creating this clock
	 * 		Variable size - the #PIPE_SIZE variable
	 * 		HashMap<String, Variable> vars - the list of all variables in the current debugger
	 * 
	 * 	Returns:
	 * 		A newly created clock or null if the clock could not be created
	 */
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
			c.printLocation();
			return c;
		} else {
			p.sendMessage(RSDB.prefix + "Unable to create clock: The clock selection must be a block of glass.");
			return null;
		}
	}
	
	/*
	 * 	Parameters:
	 * 		Location l - the location of this clock
	 * 		RSDB rsdb - the main plugin instance
	 * 		Player p - the player who is creating this clock
	 * 		HashMap<String, Variable> vars - the list of all variables in the current debugger
	 * 
	 * 	Description:
	 * 		Clock constructor
	 */
	private Clock(Location l, RSDB rsdb, Player p, Variable size, HashMap<String, Variable> vars) {
		super(l, rsdb, p, "#CLOCK", size, Material.GLASS);
		this.vars = vars;
	}
	
	/*
	 * 	Parameters:
	 * 		int numTicks - the number of ticks which the clock should pulse for
	 * 
	 * 	Returns:
	 * 		True if the pulse was successful
	 */
	public boolean pulse(int numTicks) {
		for(Variable v : vars.values()) {
			if(v != null)
				v.update();
		}
		return super.pulse(numTicks, Material.GLASS);
	}
	
	/*
	 * 	Returns:
	 * 		True if the clock was successfully toggled
	 */
	public boolean toggle() {
		if(super.l.get(0).getBlock().getType().equals(Material.GLASS)) {
			for(Variable v : vars.values()) {
				if(v != null)
					v.update();
			}
		}
		
		if(!super.toggle()) {
			p.sendMessage(RSDB.errorPrefix + "Unable to trigger clock: The clock source was modified.");
			return false;
		}
		
		return true;
	}
	
	/*
	 * Returns:
	 * 	True if the clock can legally pulse	
	 * 
	 * @see com.gmail.redstonebunny.rsdb.variables.Output#validatePulse()
	 */
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
