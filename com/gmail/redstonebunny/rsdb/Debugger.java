package com.gmail.redstonebunny.rsdb;

import java.util.HashMap;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import com.gmail.redstonebunny.rsdb.script.Clock;
import com.gmail.redstonebunny.rsdb.script.Reset;
import com.gmail.redstonebunny.rsdb.script.Watchpoint;

public class Debugger {
	private Player p;
	private RSDB rsdb;
	private Clock clock;
	private Reset reset;
	private HashMap<String, Watchpoint> watchpoints;
	
	public Debugger(Player player, RSDB rsdb) {
		this.p = player;
		this.rsdb = rsdb;
		this.watchpoints = new HashMap<String, Watchpoint>();
		player.sendMessage(RSDB.successPrefix + "Successfully created a new debugger.");
	}
	
	public void processCommand(String args[]) {
		if(args[0].equalsIgnoreCase("watch")) {
			if(args.length == 2) {
				Watchpoint w = watchpoints.get(args[1]);
				if(w != null) {
					w.appendSensors(p);
				} else {
					w = Watchpoint.createWatchpoint(args[1], p);
					if(w != null)
						watchpoints.put(args[1], w);
				}
					
			} else {
				p.sendMessage(RSDB.prefix + "Invalid format. Use \"/rsdb watch <watchpoint_name>\".");
			}
		} else if(args[0].equalsIgnoreCase("print")) {
			commandPrint(args);
		} else if(args[0].equalsIgnoreCase("delete")) {
			commandDelete(args);
		} else if(args[0].equalsIgnoreCase("set")) {
			commandSet(args);
		} else if(args[0].equalsIgnoreCase("step")) {
			commandStep(args);
		} else if(args[0].equalsIgnoreCase("reset")) {
			commandReset(args);
		}
		else {
			p.sendMessage(RSDB.prefix + "Unknown command \"" + args[0] + "\".");
		}
	}
	
	private void commandReset(String args[]) {
		if(args.length == 1) {
			if(reset != null)
				reset.pulse(5);
			
			if(clock != null)
				clock.resetCount();
		} 
		else {
			p.sendMessage(RSDB.prefix + "Invalid format. Use \"/rsdb reset\".");
		}
	}
	
	private void commandStep(String args[]) {
		if(args.length == 1) {
			if(clock != null) {
				clock.pulse(5);
			} 
			else {
				p.sendMessage(RSDB.prefix + "You cannot use the step command without creating a clock.");
			}
		} else if(args.length == 2) {
			if(contains(args, "--toggle")) {
				clock.toggle();
			} 
			else {
				p.sendMessage(RSDB.prefix + "Invalid format: Use \"/rsdb step\".");
			}
		} 
		else {
			p.sendMessage(RSDB.prefix + "Invalid format: Use \"/rsdb step\".");
		}
	}
	
	private void commandSet(String args[]) {
		if(args.length == 2) {
			if(args[1].equalsIgnoreCase("clock")) {
				Clock tmp = Clock.createClock(rsdb, p);
				if(tmp != null)
					clock = tmp;
			} else if(args[1].equalsIgnoreCase("reset")) {
				Reset tmp = Reset.createReset(rsdb, p);
				if(tmp != null)
					reset = tmp;
			} 
			else {
				p.sendMessage(RSDB.prefix + "Unknown component: \"" + args[1] + "\".");
			}
		} 
		else {
			p.sendMessage(RSDB.prefix + "Invalid format. Use \"/rsdb set <component_name>\".");
		}
	}
	
	private void commandDelete(String args[]) {
		if(args.length == 2) {
			if(watchpoints.remove(args[1]) == null) {
				p.sendMessage(RSDB.prefix + "Could not find watchpoint \"" + args[1] + "\".");
			} 
			else {
				p.sendMessage(RSDB.successPrefix + "Successfully removed watchpoint \"" + args[1] + "\"");
			}
		} 
		else {
			p.sendMessage(RSDB.prefix + "Invalid format. Use \"/rsdb delete <watchpoint_name>\".");
		}
	}
	
	private void commandPrint(String args[]) {
		if(args.length == 1) {
			if(watchpoints.size() == 0)
				p.sendMessage(RSDB.prefix + "The current debugger does not have any watchpoints to print.");
			for(Watchpoint wp : watchpoints.values()) {
				wp.printValue(p);
			}
		} else if(args.length == 2) {
			if(args[1].startsWith("$")) {
				if(args[1].charAt(1) == '#') {
					String str = args[1].substring(2);
					if(str.equalsIgnoreCase("clock")) {
						clock.printCount(p);
					} 
					else {
						p.sendMessage(RSDB.prefix + "Could not find variable \"" + args[1] + "\".");
					}
				} 
				else {
					Watchpoint wp = watchpoints.get(args[1].substring(1));
					if(wp == null) {
						p.sendMessage(RSDB.prefix + "Could not find variable \"" + args[1] + "\".");
					} 
					else {
						wp.printValue(p);
					}
				}
			} 
			else {
				if(args[1].startsWith("#")) {
					String str = args[1].substring(1);
					if(str.equalsIgnoreCase("clock") && clock != null) {
						clock.print();
					} 
					else {
						p.sendMessage(RSDB.prefix + "Could not find variable \"" + args[1] + "\".");
					}
				} 
				else {
					Watchpoint wp = watchpoints.get(args[1]);
					if(wp == null) {
						p.sendMessage(RSDB.prefix + "Could not find variable \"" + args[1] + "\".");
					} 
					else {
						wp.printBitLocations(p);
					}
				}
			}
		} 
		else {
			p.sendMessage(RSDB.prefix + "Invalid format. Use \"/rsdb print <variable_name>\".");
		}
	}
	
	public static void help(String[] args, Player p) {
		if(args == null || args.length < 2 || args[1].equals("1")) {
			p.sendMessage(
				RSDB.prefix + "Command Overview (" + ChatColor.DARK_GRAY + "Page 1" + ChatColor.GRAY + "): \n" +
				RSDB.prefix + "/rsdb " + ChatColor.GOLD + "create" + ChatColor.GRAY + " - Create a debugger object - /rsdb help create\n" +
				RSDB.prefix + "/rsdb " + ChatColor.GOLD + "watch" + ChatColor.GRAY + " - Create a watchpoint for analyzing redstone signals - /rsdb help watch\n" +
				RSDB.prefix + "/rsdb " + ChatColor.GOLD + "print" + ChatColor.GRAY + " - Displays watchpoint data - /rsdb help print\n" +
				RSDB.prefix + "/rsdb " + ChatColor.GOLD + "delete" + ChatColor.GRAY + " - Deletes a watchpoint - /rsdb help delete\n" +
				RSDB.prefix + "/rsdb " + ChatColor.GOLD + "set" + ChatColor.GRAY + " - Sets a system component - /rsdb help set"
			);
		} else if(args[1].equals("2")) {
			p.sendMessage(
				RSDB.prefix + "Command Overview (" + ChatColor.DARK_GRAY + "Page 2" + ChatColor.GRAY + "): \n" +
				RSDB.prefix + "/rsdb " + ChatColor.GOLD + "step" + ChatColor.GRAY + " - Pulses the clock a single time - /rsdb help next\n"
			);
		} else if(args[1].equalsIgnoreCase("create")){
			p.sendMessage(
					RSDB.prefix + "Use the command \"/rsdb create\" to create a new redstone debugger. " +
					"This command will delete any debugger that you may currently have, including it's contents. Use with caution."
			);
		} else if(args[1].equalsIgnoreCase("watch")) {
			p.sendMessage(
				RSDB.prefix + "Use the command \"/rsdb watch <watchpoint_name>\" to create a watchpoint with a certain name. Your worldedit " +
					"selection is used to generate the watchpoint with each redstone wire in your selection being treated as a individual bit. " +
					"The MSB of your selection is the farthest bit in the direction you are facing. If a watchpoint with the specified name already " +
					"exists then the selection is appended onto the existing watchpoint."
			);
		} else if(args[1].equalsIgnoreCase("print")) {
			p.sendMessage(
				RSDB.prefix + "Use the command \"/rsdb print\" to print the values of all watchpoints in the current debugger. Use the command " +
				"\"/rsdb print <variable_name>\" to print the materials and coordinates attributed to that variable. Use the command \"/rsdb print " +
				"$<variable_name>\" to print the value of the variable."
			);
		} else if(args[1].equalsIgnoreCase("delete")) {
			p.sendMessage(
				RSDB.prefix + "Use the command \"/rsdb delete <watchpoint_name>\" to remove a watchpoint."
			);
		} else if(args[1].equalsIgnoreCase("set")) {
			if(args.length == 2) {
				p.sendMessage(
						RSDB.prefix + "Use the command \"/rsdb set <component_name>\" to set a component.\nComponents which can be set: " +
						"clock (/rsdb help set clock), reset (/rsdb help set reset)"
				);
			} else if(args.length == 3) {
				if(args[2].equalsIgnoreCase("clock")) {
					p.sendMessage(
							RSDB.prefix + "Use the command \"/rsdb set clock\" to create a clock. The clock is created at the location of the players worldedit " +
							"selection. The selction must be a 1 x 1 x 1 block of glass which touches your clock wire.  The clock variable is stored in \"#CLOCK\"."
					);
				} else if(args[2].equalsIgnoreCase("reset")) {
					
				} 
				else {
					p.sendMessage(RSDB.prefix + "Unknow help set command.");
				}
			} 
			else {
				p.sendMessage(RSDB.prefix + "Unknow help set command.");
			}
		} else if(args[1].equalsIgnoreCase("step")) {
			p.sendMessage(
				RSDB.prefix + "Use the command \"/rsdb step\" to pulse the clock a single time with a 5 tick pulse. Use \"/rsdb step --toggle\" to toggle " +
				"the state of the clock."
			);
		} 
		else {
			p.sendMessage(RSDB.prefix + "Unknown help command.");
		}
	}
	
	private boolean contains(String args[], String str) {
		for(String s : args) {
			if(str.equalsIgnoreCase(s))
				return true;
		}
		
		return false;
	}
}
