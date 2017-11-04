package com.gmail.redstonebunny.rsdb.script;

import org.bukkit.entity.Player;

public class Script {
	
	public static Script createScript(String url, Player p) {
		String str = ScriptInterface.getScript(url, p);
		if(str == null)
			return null;
		
		return new Script(str, p);
	}
	
	public Script(String script, Player p) {
		
	}
}
