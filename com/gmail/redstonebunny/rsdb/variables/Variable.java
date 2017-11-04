package com.gmail.redstonebunny.rsdb.variables;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import com.gmail.redstonebunny.rsdb.RSDB;

public class Variable {
	private ArrayList<Integer> pastValues;
	protected String name;
	protected ArrayList<Location> l;
	protected Variable size;
	protected int currentValue;
	
	public static boolean isLegalVarName(String name) {
		Pattern ptrn = Pattern.compile("^[a-zA-Z0-9_]*$");
		Matcher m = ptrn.matcher(name);
		
		return m.find();
	}
	
	public Variable(String name, int initial) {
		this.name = name;
		this.currentValue = initial;
		this.l = null;
		this.size = this;
		pastValues = new ArrayList<Integer>();
	}
	
	public Variable(String name, Variable size) {
		this.name = name;
		this.l = null;
		this.size = size;
		pastValues = new ArrayList<Integer>();
	}
	
	public Variable(String name, Location l, Variable size) {
		this.name = name;
		this.l = new ArrayList<Location>();
		this.l.add(l);
		this.size = size;
		pastValues = new ArrayList<Integer>();
	}
	
	public Variable(String name, ArrayList<Location> l, Variable size) {
		this.name = name;
		this.l = l;
		this.size = size;
		pastValues = new ArrayList<Integer>();
	}
	
	public void printValue(Player p) {
		
		p.sendMessage(RSDB.prefix + ChatColor.GOLD + "$" + name + ChatColor.GRAY + " = " + getValue() + " <-> " + Integer.toBinaryString(getValue()) + "b");
	}
	
	public void printLocation(Player p) {
		if(l != null) {
			for(int i = 0; i < l.size(); i++)
				p.sendMessage(RSDB.prefix + name + ": x=" + l.get(i).getBlockX() + " y=" + l.get(i).getBlockY() + " z=" + l.get(i).getBlockZ() + " type=" + l.get(i).getBlock().getType());
		}
		else {
			p.sendMessage(RSDB.prefix + name + " has no coordinate representation.");
		}
	}
	
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
	
	public Integer getValue() {
		return currentValue;
	}
	
	public Integer getPastValue(int index) {
		if(pastValues.size() <= index || index < 0)
			return null;
		return pastValues.get(index);
	}
	
	public void reset() {
		pastValues.clear();
	}
	
	public void setValue(int value) {
		currentValue = value;
	}
	
	public int getNumberOfBits() {
		if(l != null) {
			return l.size();
		}
		return 1;
	}
	
	public int getSize() {
		return pastValues.size();
	}
	
	public void update() {
		updateChildren();
		pastValues.add(0, currentValue);
		while(pastValues.size() > size.getValue()) {
			pastValues.remove(pastValues.size() - 1);
		}
	}
	
	protected void updateChildren() {
		
	}
}
