package com.gmail.redstonebunny.rsdb.variables;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import com.gmail.redstonebunny.rsdb.RSDB;

/*
 * 	Output class - used for outputting signals from the debugger into the minecraft world
 */

public abstract class Output extends Variable {
	protected RSDB rsdb;	// Main plugin instance
	protected Material m;	// Material that is swapped with a redstone block
	
	// Array used for checking all points around a certain block
	protected static final Vector[] surround = {new Vector(0, 0, 1), new Vector(0, 0, -1), new Vector(0, -1, 0), new Vector(1, 0, 0), new Vector(-1, 0, 0)};
	
	/*
	 * 	Parameters:
	 * 		Location l - the location of this output
	 * 		RSDB rsdb - the main plugin instance
	 * 		String name - the name of this output
	 * 		Variable size - the #PIPE_SIZE variable
	 */
	protected Output(Location l, RSDB rsdb, Player p, String name, Variable size, Material m) {
		super(name, l, size, p);
		this.rsdb = rsdb;
		this.m = m;
	}
	
	/*
	 * 	Parameters:
	 * 		int numTicks - the number of ticks which the output should pulse
	 * 		final Material replace - the material which the output should become after the pulse
	 * 
	 * 	Returns:
	 * 		True if the pulse was successful
	 */
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
	
	
	/*
	 * 	Parameters:
	 * 		final Material replace - the material to swap with a redstone block during the toggle
	 * 
	 * 	Returns:
	 * 		True if the toggle was successful
	 */
	public boolean toggle() {
		if(l.get(0).getBlock().getType().equals(m)) {
			l.get(0).getBlock().setType(Material.REDSTONE_BLOCK);
			currentValue++;
			return true;
		} else if(l.get(0).getBlock().getType().equals(Material.REDSTONE_BLOCK)) {
			l.get(0).getBlock().setType(m);
			return true;
		}
		
		return false;
	}
	
	/*
	 * 	Returns:
	 * 		True if the output is currently on
	 */
	public boolean getState() {
		return l.get(0).getBlock().getType().equals(Material.REDSTONE_BLOCK);
	}
	
	/*
	 * 	Description:
	 * 		Overrides setValue so output variables cannot be set directly
	 * 
	 * @see com.gmail.redstonebunny.rsdb.variables.Variable#setValue(int)
	 */
	@Override
	public void setValue(int value) {
		
	}
	
	/*
	 * 	Description:
	 * 		Resets the outputs counter
	 * 
	 * @see com.gmail.redstonebunny.rsdb.variables.Variable#reset()
	 */
	@Override
	public void reset() {
		super.reset();
		currentValue = 0;
	}
	
	// Used by subclasses to insure the output is in the correct state before pulsing
	protected abstract boolean validatePulse();
}
