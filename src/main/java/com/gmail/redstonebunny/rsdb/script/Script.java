package com.gmail.redstonebunny.rsdb.script;

import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.entity.Player;

/*
 * 	Script class - holds and executes a script
 */

public class Script {
	
	HashMap<String, ArrayList<String>> sections;
	
	/*
	 * 	Parameters:
	 * 		String url - the url to a pastee which holds the script to be loaded
	 * 		Player p - the player who is loading this script
	 * 
	 * 	Returns:
	 * 		A script object or null if the script could not be loaded
	 */
	public static Script createScript(String url, Player p) {
		String str = ScriptInterface.getScript(url, p);
		if(str == null)
			return null;
		
		return new Script(str, p);
	}
	
	public Script(String script, Player p) {
		
	}
}
