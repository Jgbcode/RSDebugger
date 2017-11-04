package com.gmail.redstonebunny.rsdb.variables;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import com.gmail.redstonebunny.rsdb.RSDB;

/*
 * 	Variable class - a general purpose variable class
 */

public class Variable {
	private ArrayList<Integer> pastValues;	// The past values of the variable (updated on each clock pulse)
	protected String name;	// The name of the variable
	protected ArrayList<Location> l;	// The location of the variable (null if none)
	protected Variable size;	// #PIPE_SIZE variable which directly controls the size of pastValues
	protected int currentValue;	// The current value of the variable
	
	/*
	 * 	Parameters:
	 * 		String name - the name of a potential variable
	 * 
	 * 	Returns:
	 * 		True if the name is legal
	 */
	public static boolean isLegalVarName(String name) {
		Pattern ptrn = Pattern.compile("^[a-zA-Z0-9_]*$");
		Matcher m = ptrn.matcher(name);
		
		return m.find();
	}
	
	/*
	 * 	Parameters:
	 * 		String name - the name of the variable
	 * 		int initial - the initial value of the variable
	 * 
	 * 	Description:
	 * 		Variable constructor, this constructor is specifically designed for #PIPE_SIZE
	 */
	public Variable(String name, int initial) {
		this.name = name;
		this.currentValue = initial;
		this.l = null;
		this.size = this;
		pastValues = new ArrayList<Integer>();
	}
	
	/*
	 * 	Parameters:
	 * 		String name - the name of the variable
	 * 		Variable size - the #PIPE_SIZE variable
	 * 
	 * 	Description:
	 * 		Variable constructor for variables with no location
	 */
	public Variable(String name, Variable size) {
		this.name = name;
		this.l = null;
		this.size = size;
		pastValues = new ArrayList<Integer>();
	}
	
	/*
	 * 	Parameters:
	 * 		String name - the name of the variable
	 * 		Location l - the location of the variable
	 * 		Variable size - the #PIPE_SIZE variable
	 * 
	 * 	Description:
	 * 		Variable constructor for variables with a single location
	 */
	public Variable(String name, Location l, Variable size) {
		this.name = name;
		this.l = new ArrayList<Location>();
		this.l.add(l);
		this.size = size;
		pastValues = new ArrayList<Integer>();
	}
	
	/*
	 * 	Parameters:
	 * 		String name - the name of the variable
	 * 		ArrayList<Location> l - the locations of the variable
	 * 		Variable size - the #PIPE_SIZE variable
	 * 
	 * 	Description:
	 * 		Variable constructor for variables with many locations
	 */
	public Variable(String name, ArrayList<Location> l, Variable size) {
		this.name = name;
		this.l = l;
		this.size = size;
		pastValues = new ArrayList<Integer>();
	}
	
	/*
	 * 	Parameters:
	 * 		Player p - the player to display this variables info to
	 * 
	 * 	Description:
	 * 		Prints the variable's value
	 */
	public void printValue(Player p) {
		
		p.sendMessage(RSDB.prefix + ChatColor.GOLD + "$" + name + ChatColor.GRAY + " = " + getValue() + " <-> " + Integer.toBinaryString(getValue()) + "b");
	}
	
	/*
	 * 	Parameters:
	 * 		Player p - the player to display this variables info to
	 * 
	 * 	Description:
	 * 		Prints the variable's location if it has one
	 */
	public void printLocation(Player p) {
		if(l != null) {
			for(int i = 0; i < l.size(); i++)
				p.sendMessage(RSDB.prefix + name + "(bit" + i + ") : x=" + l.get(i).getBlockX() + " y=" + l.get(i).getBlockY() + " z=" + l.get(i).getBlockZ() + " type=" + l.get(i).getBlock().getType());
		}
		else {
			p.sendMessage(RSDB.prefix + name + " has no coordinate representation.");
		}
	}
	
	/*
	 * 	Returns:
	 * 		An array of each location this variable has
	 */
	public Location[] getLocation() {
		if(l != null) {
			Location[] v = new Location[l.size()];
			for(int i = 0; i < l.size(); i++) {
				v[i] = l.get(i).clone();
			}
			return v;
		}
			
		return null;
	}
	
	/*
	 * 	Returns:
	 * 		The current value of this variable
	 */
	public Integer getValue() {
		return currentValue;
	}
	
	/*
	 * 	Returns:
	 * 		A past value of this variable or null if the index is out of bounds
	 */
	public Integer getPastValue(int index) {
		if(pastValues.size() <= index || index < 0)
			return null;
		return pastValues.get(index);
	}
	
	/*
	 * 	Description:
	 * 		Resets this variable
	 */
	public void reset() {
		pastValues.clear();
	}
	
	/*
	 * 	Parameters:
	 * 		int value - the value this variable is to be set to
	 */
	public void setValue(int value) {
		currentValue = value;
	}
	
	/*
	 * 	Returns:
	 * 		The number of bits this variable is
	 */
	public int getNumberOfBits() {
		if(l != null) {
			return l.size();
		}
		return 1;
	}
	
	/*
	 * 	Returns:
	 * 		The number of past values available
	 */
	public int getSize() {
		return pastValues.size();
	}
	
	/*
	 * 	Description:
	 * 		Updates the variable on a clock pulse
	 */
	public void update() {
		updateChildren();
		pastValues.add(0, getValue());
		while(pastValues.size() > size.getValue()) {
			pastValues.remove(pastValues.size() - 1);
		}
	}
	/*
	 * 	Description:
	 * 		To be inherited by subclasse to update themselves
	 */
	protected void updateChildren() {
		
	}
}
