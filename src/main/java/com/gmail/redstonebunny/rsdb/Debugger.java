package com.gmail.redstonebunny.rsdb;

import java.util.HashMap;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import com.gmail.redstonebunny.rsdb.script.Parser;
import com.gmail.redstonebunny.rsdb.script.Script;
import com.gmail.redstonebunny.rsdb.variables.Clock;
import com.gmail.redstonebunny.rsdb.variables.Operator;
import com.gmail.redstonebunny.rsdb.variables.Output;
import com.gmail.redstonebunny.rsdb.variables.Reset;
import com.gmail.redstonebunny.rsdb.variables.Variable;
import com.gmail.redstonebunny.rsdb.variables.Watchpoint;

/*
 * 	Debugger class - holds a players debugging information and services commands
 */

public class Debugger {
	private Player p;	// The player who this debugger belongs to
	private RSDB rsdb;	// The main plugin instance
	private HashMap<String, Variable> variables;	// A map for converting variable names into variable objects
	private Script script;
	
	public static Debugger createDebugger(Player player, RSDB rsdb, String url) {
		HashMap<String, Variable> vars = new HashMap<String, Variable>();
		vars.put("#PIPE_SIZE", new Variable("#PIPE_SIZE", 10, player));
		vars.put("#CLOCK", null);
		vars.put("#RESET", null);
		Script script = Script.createScript(url, player, vars);
		if(script == null)
			return null;
		return new Debugger(player, rsdb, vars, script);
	}
	
	private Debugger(Player player, RSDB rsdb, HashMap<String, Variable> variables, Script script) {
		this.p = player;
		this.rsdb = rsdb;
		this.variables = variables;
		this.script = script;
		player.sendMessage(RSDB.successPrefix + "Successfully created a new debugger using a script.");
	}
	
	/*
	 * 	Parameters:
	 * 		Player player - the player who created this debugger instance
	 * 		RSDB rsdb - the main plugin instance
	 * 
	 * 	Description:
	 * 		Debugger constructor
	 */
	public Debugger(Player player, RSDB rsdb) {
		this.p = player;
		this.rsdb = rsdb;
		this.variables = new HashMap<String, Variable>();
		variables.put("#PIPE_SIZE", new Variable("#PIPE_SIZE", 10, p));
		variables.put("#CLOCK", null);
		variables.put("#RESET", null);
		player.sendMessage(RSDB.successPrefix + "Successfully created a new debugger.");
	}
	
	public void disable() {
		for(Variable v : variables.values()) {
			if(v instanceof Output && ((Output)v).getState()) {
				((Output)v).toggle();
			}
		}
	}
	
	/*
	 * 	Parameters:
	 * 		String args[] - command arguments
	 * 
	 * 	Description:
	 * 		This function services a majority of "/rsdb" commands. The remaining commands are serviced in RSDB.onCommand
	 */
	public void processCommand(String args[]) {
		if(args[0].equalsIgnoreCase("watch")) {
			commandWatch(args);
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
	
	/*
	 * 	Parameters:
	 * 		String args[] - command arguments
	 * 
	 * 	Description:
	 * 		Services "/rsdb watch" commands
	 */
	private void commandWatch(String args[]) {
		if(args.length == 2) {
			Variable w = variables.get(args[1]);
			if(w != null && !(w instanceof Watchpoint))
				p.sendMessage(RSDB.prefix + args[1] + " is not a watchpoint.");
			
			if(w != null) {
				((Watchpoint)w).appendSensors(p);
			} else {
				w = Watchpoint.createWatchpoint(args[1], p, variables.get("#PIPE_SIZE"));
				if(w != null)
					variables.put(args[1], w);
			}	
		} else {
			p.sendMessage(RSDB.prefix + "Invalid format. Use \"/rsdb watch <watchpoint_name>\".");
		}
	}
	
	/*
	 * 	Parameters:
	 * 		String args[] - command arguments
	 * 
	 * 	Description:
	 * 		Services "/rsdb reset" commands
	 */
	private void commandReset(String args[]) {
		if(args.length == 1) {
			if(variables.get("#RESET") != null)
				((Reset)variables.get("#RESET")).pulse(5);
			
			for(Variable v : variables.values()) {
				if(v != null)
					v.reset();
			}	
		} 
		else {
			p.sendMessage(RSDB.prefix + "Invalid format. Use \"/rsdb reset\".");
		}
	}
	
	/*
	 * 	Parameters:
	 * 		String args[] - command arguments
	 * 
	 * 	Description:
	 * 		Services "/rsdb step" commands
	 */
	private void commandStep(String args[]) {
		if(variables.get("#CLOCK") == null) {
			p.sendMessage(RSDB.prefix + "You cannot use the step command without creating a clock.");
			return;
		}
		
		if(args.length == 1) {
			((Clock)variables.get("#CLOCK")).pulse(5);
		} else if(args.length == 2) {
			if(args[1].equals("--toggle")) {	
				((Clock)variables.get("#CLOCK")).toggle();
			} 
			else {
				p.sendMessage(RSDB.prefix + "Invalid format: Use \"/rsdb step\".");
			}
		} 
		else {
			p.sendMessage(RSDB.prefix + "Invalid format: Use \"/rsdb step\".");
		}
	}
	
	/*
	 * 	Parameters:
	 * 		String args[] - command arguments
	 * 
	 * 	Description:
	 * 		Services "/rsdb set" commands
	 */
	private void commandSet(String args[]) {
		if(args.length == 2) {
			if(args[1].equals("#CLOCK")) {
				Clock tmp = Clock.createClock(rsdb, p, variables.get("#PIPE_SIZE"), variables, script);
				if(tmp != null)
					 variables.put("#CLOCK", tmp);
			} else if(args[1].equals("#RESET")) {
				Reset tmp = Reset.createReset(rsdb, p, variables.get("#PIPE_SIZE"));
				if(tmp != null)
					variables.put("#RESET", tmp);
			} 
			else {
				p.sendMessage(RSDB.prefix + "Unknown component: \"" + args[1] + "\".");
			}
		} else if(args.length == 3 && (args[1].startsWith("$") || args[1].startsWith("@"))) {
			Integer tmp = Parser.getValue(args[2], variables, p);
			if(tmp == null) {
				p.sendMessage(RSDB.prefix + "Failed to set variable: Illegal value.");
			}
			
			 	if(args[1].charAt(1) == '#') {
					if(variables.containsKey(args[1].substring(1)) && variables.get(args[1].substring(1)) != null) {
						variables.get(args[1].substring(1)).setValue(tmp);
					}
					else {
						p.sendMessage(RSDB.prefix + "Failed to set variable: System variable does not exist.");
						return;
					}
				} else {
					if(variables.containsKey(args[1].substring(1))) {
						variables.get(args[1].substring(1)).setValue(tmp);
						p.sendMessage(RSDB.successPrefix + "Successfully set \"" + args[1] + "\" to " + args[2] + ".");
					} else if(args[1].startsWith("$") && Variable.isLegalVarName(args[1].substring(1))) {
						variables.put(args[1].substring(1), new Variable(args[1].substring(1), variables.get("#PIPE_SIZE"), p));
						p.sendMessage(RSDB.successPrefix + "Successfully set \"" + args[1] + "\" to " + args[2] + ".");
					} else if(args[1].startsWith("@")) {
						Operator op = Operator.createOperator(args[1], variables.get("#PIPE_SIZE"), p, tmp);
						if(op != null) {
							variables.put(args[1], op);
							p.sendMessage(RSDB.successPrefix + "Successfully set \"" + args[1] + "\" to " + args[2] + ".");
						}
					}
					else {
						p.sendMessage(RSDB.prefix + "Failed to set variable: Variable name contains illegal characters.");
						return;
					}
				}
		}
		else {
			p.sendMessage(RSDB.prefix + "Invalid format. Use \"/rsdb set <variable_name> <optional_value>\".");
		}
	}
	
	/*
	 * 	Parameters:
	 * 		String args[] - command arguments
	 * 
	 * 	Description:
	 * 		Services "/rsdb delete" commands
	 */
	private void commandDelete(String args[]) {
		if(args.length == 2) {
			if(args[1].equals("#PIPE_SIZE")) {
				p.sendMessage(RSDB.prefix + "Deleting #PIPE_SIZE will corrupt your debugger. This action is disabled.");
			} else if(variables.get(args[1]) == null) {
				p.sendMessage(RSDB.prefix + "Could not find variable \"" + args[1] + "\".");
			} 
			else {
				variables.put(args[1], null);
				p.sendMessage(RSDB.successPrefix + "Successfully removed variable \"" + args[1] + "\"");
			}
		} 
		else {
			p.sendMessage(RSDB.prefix + "Invalid format. Use \"/rsdb delete <variable_name>\".");
		}
	}
	
	/*
	 * 	Parameters:
	 * 		String args[] - command arguments
	 * 
	 * 	Description:
	 * 		Services "/rsdb print" commands
	 */
	private void commandPrint(String args[]) {
		if(args.length == 1) {
			for(Variable v : variables.values()) {
				if(v == null)
					continue;
				v.printValue();
			}
		} else if(args.length == 2) {
			if(!variables.containsKey(args[1])) {
				Integer value = Parser.evaluateExpression(args[1], variables, p);
				
				if(value != null) {
					p.sendMessage(RSDB.prefix + ChatColor.GOLD + args[1] + ChatColor.GRAY + " = " + value + " : " + Integer.toBinaryString(value) + "b");
				}
				else {
					p.sendMessage(RSDB.prefix + "Could not find variable \"" + args[1] + "\".");
				}
			} 
			else {
				if(variables.get(args[1]) != null) {
					variables.get(args[1]).printLocation();
				}
				else {
					p.sendMessage(RSDB.prefix + "Could not find variable \"" + args[1] + "\".");
				}
			}
		} 
		else {
			p.sendMessage(RSDB.prefix + "Invalid format. Use \"/rsdb print <variable_name>\".");
		}
	}
	
	/*
	 * 	Parameters:
	 * 		String[] args - command arguments
	 * 		Player p - the player who issued the command
	 * 
	 * 	Description:
	 * 		Services "/rsdb help" commands
	 */
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
						RSDB.prefix + "Use the command \"/rsdb set <component_name>\" to set a components value."
				);
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
}
