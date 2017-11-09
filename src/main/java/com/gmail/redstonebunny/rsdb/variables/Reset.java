package com.gmail.redstonebunny.rsdb.variables;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.material.RedstoneWire;

import com.gmail.redstonebunny.rsdb.RSDB;
import com.gmail.redstonebunny.rsdb.WorldEditHelper;
import com.sk89q.worldedit.bukkit.selections.Selection;

/*
 * 	Reset class - interacts with the minecraft world to reset the circuit
 */

public class Reset extends Output {
	// The player who created this reset
	private Player p;
	
	/*
	 * 	Parameters:
	 * 		RSDB rsdb - the main plugin instance
	 * 		Player p - the player who is creating this reset
	 * 		Variable size - the #PIPE_SIZE variable
	 * 
	 * 	Returns:
	 * 		A reset object or null if the provided arguments were invalid
	 */
	public static Reset createReset(RSDB rsdb, Player p, Variable size) {
		Selection s = WorldEditHelper.getSelection(p);
		
		if(s == null)
			return null;
		
		Location l;
		if(s.getWidth() == 1 && s.getLength() == 1 && s.getHeight() == 1) {
			l = s.getMinimumPoint();
		} else {
			p.sendMessage(RSDB.prefix + "Unable to create reset: The reset selection must be a single block.");
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
			p.sendMessage(RSDB.prefix + "Unable to create reset: The selected block is not connected to a redstone wire.");
			return null;
		}
		
		if(l.getBlock().getType().equals(Material.GLASS)) {
			Reset r = new Reset(l, rsdb, p, size);
			p.sendMessage(RSDB.successPrefix + "Successfully created a reset:");
			r.printLocation();
			return r;
		} else {
			p.sendMessage(RSDB.prefix + "Unable to create reset: The reset selection must be a block of glass.");
			return null;
		}
	}
	
	/*
	 * 	Parameters:
	 * 		Location l - the location of the reset output
	 * 		RSDB rsdb - the main plugin instance
	 * 		Player p - the player who is creating this reset
	 * 		Variable size - the #PIPE_SIZE variable
	 */
	protected Reset(Location l, RSDB rsdb, Player p, Variable size) {
		super(l, rsdb, p, "#RESET", size, Material.GLASS);
		this.p = p;
	}

	/*
	 * 	Returns:
	 * 		True if the reset can legally pulse
	 * 
	 * @see com.gmail.redstonebunny.rsdb.variables.Output#validatePulse()
	 */
	@Override
	protected boolean validatePulse() {
		if(!l.get(0).getBlock().getType().equals(Material.GLASS)) {
			p.sendMessage(RSDB.errorPrefix + "Unable to trigger reset: The reset source was modified.");
			return false;
		}
		
		return true;
	}
	
	/*
	 * 	Parameters:
	 * 		int numTicks - the number of ticks the reset should pulse for
	 * 	
	 * 	Returns:
	 * 		True if the pulse was successful
	 */
	public boolean pulse(int numTicks) {
		return super.pulse(numTicks, Material.GLASS);
	}
}
