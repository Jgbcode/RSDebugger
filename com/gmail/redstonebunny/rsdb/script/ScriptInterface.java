package com.gmail.redstonebunny.rsdb.script;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import com.gmail.redstonebunny.rsdb.RSDB;

import fr.minuskube.pastee.JPastee;
import fr.minuskube.pastee.data.Paste;
import fr.minuskube.pastee.data.Section;
import fr.minuskube.pastee.response.PasteResponse;
import fr.minuskube.pastee.response.SubmitResponse;

public class ScriptInterface {
	private static JPastee pastee;
	public static final String prefix = ChatColor.DARK_RED + "[" + ChatColor.DARK_PURPLE + "SCRIPT" + ChatColor.DARK_RED +"] " + ChatColor.GRAY;
	public static final String successPrefix = prefix + ChatColor.GREEN;
	public static final String errorPrefix = prefix + ChatColor.RED;
	
	public static void sendScript(String script, Player p) {
		if(pastee == null)
			pastee = new JPastee("uYS1EZBouiieRKef0ksf6GLO6al5l1mQ9qNo2K2jq");
		
		Paste paste = Paste.builder()
				.description("RSDB Script for " + p.getName())
				.addSection(Section.builder().name("Script").contents(script).build())
				.build();
		
		SubmitResponse resp = pastee.submit(paste);
		
		if(resp.isSuccess()) {
			p.sendMessage(RSDB.prefix + successPrefix + "Successfully generated the script at " + ChatColor.GOLD + resp.getLink());
		} else {
			p.sendMessage(RSDB.prefix + errorPrefix + "Unable to generate the script: Use \"/rsdb script --dump\" to display the script via the chat.");
		}
	}
	
	public static String getScript(String url, Player p) {
		if(pastee == null)
			pastee = new JPastee("uYS1EZBouiieRKef0ksf6GLO6al5l1mQ9qNo2K2jq");
		
		while(url.endsWith("/")) {
			url = url.substring(0, url.length() - 1);
		}
		
		if(url.indexOf('/') > 0) {
			url = url.substring(url.lastIndexOf('/') + 1);
		}
		
		PasteResponse resp = pastee.getPaste(url);
		
		if(!resp.isSuccess()) {
			p.sendMessage(RSDB.prefix + errorPrefix + "Failed to get script. Did you enter the pastee URL correctly?");
			return null;
		}
		
		p.sendMessage(RSDB.prefix + successPrefix + "Successfully fetched script from " + url + ".");
		
		Paste paste = resp.getPaste();
		Section section = paste.getSections().get(0);
		
		return section.getContents();
	}
}
