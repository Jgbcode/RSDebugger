package com.gmail.redstonebunny.rsdb;

import java.util.HashMap;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

/*
 * 	RSDB class - main plugin instance, stores all players debuggers
 */

public class RSDB extends JavaPlugin {
	private HashMap<UUID, Debugger> debuggers;	// All players debuggers
	
	// Prefixes for chat output
	public static final String prefix = ChatColor.BLACK + "[" + ChatColor.DARK_GREEN + "RSDB" + ChatColor.BLACK + "] " + ChatColor.GRAY;
	public static final String successPrefix = prefix + ChatColor.GREEN;
	public static final String errorPrefix = prefix + ChatColor.DARK_RED;
	
	@Override
	public void onEnable() {
		debuggers = new HashMap<UUID, Debugger>();
	}
	
	@Override
	public void onDisable() {
		for(Debugger d : debuggers.values()) {
			d.disable();
		}
		
		debuggers.clear();
		debuggers = null;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if(!(sender instanceof Player)) {
			sender.sendMessage(errorPrefix + "This command does not work from console.");
			return true;
		} else if(!((Player) sender).hasPermission("rsdebugger.use")) {
			sender.sendMessage(errorPrefix +  "You do not have permission for this command.");
			return true;
		}
		
		Player player = (Player)sender;
		
		if(args.length == 0 || (args[0].equalsIgnoreCase("help") && args.length == 1)) {
			Debugger.help(null, player);
			return true;
		} else if (args[0].equalsIgnoreCase("help") && args.length == 2) {
			Debugger.help(args, player);
			return true;
		} else if(args[0].equalsIgnoreCase("create")) {
			if(args.length == 1) {
				debuggers.put(player.getUniqueId(), new Debugger(player, this));
				return true;
			} else if(args.length == 2) {
				if(player.hasPermission("rsdebugger.load")) {
					Debugger tmp = Debugger.createDebugger(player, this, args[1]);
					if(tmp != null) {
						debuggers.put(player.getUniqueId(), tmp);
					}
				} else {
					player.sendMessage(errorPrefix + "You do not have permission for this command.");
					return true;
				}
			}
			else {
				player.sendMessage(prefix + "Too many arguments. Use \"/rsdb create\" or \"/rsdb create <script_url>\".");
			}
		} else {
			if(!debuggers.containsKey(player.getUniqueId())) {
				player.sendMessage(prefix + "No debugger found for " + player.getDisplayName() + ".  Generating debugger...");
				debuggers.put(player.getUniqueId(), new Debugger(player, this));
			}
			
			debuggers.get(player.getUniqueId()).processCommand(args);
		}
		return true;
	}
}
