package com.gmail.redstonebunny.rsdb.script;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import com.gmail.redstonebunny.rsdb.RSDB;

import fr.minuskube.pastee.JPastee;
import fr.minuskube.pastee.data.Paste;
import fr.minuskube.pastee.data.Section;
import fr.minuskube.pastee.response.PasteResponse;
import fr.minuskube.pastee.response.SubmitResponse;

/*
 * 	ScriptInterface class - interfaces between the script object and pastee
 */

public class ScriptInterface {
	// JPastee object to connect to pastee
	private static JPastee pastee;
	
	/*
	 * 	Parameters:
	 * 		String script - the script
	 * 		Player p - the player who owns this script
	 * 
	 * 	Description:
	 * 		Uploads this script to pastee and prints the link to the player
	 */
	public static boolean sendScript(String script, Player p) {
		if(pastee == null)
			pastee = new JPastee("uYS1EZBouiieRKef0ksf6GLO6al5l1mQ9qNo2K2jq");
		
		Paste paste = Paste.builder()
				.description("RSDB Script for " + p.getName())
				.addSection(Section.builder().name("Script").contents(script).syntax(pastee.getSyntaxFromName("xml").get()).build())
				.build();
		
		SubmitResponse resp = pastee.submit(paste);
		
		if(resp.isSuccess()) {
			p.sendMessage(RSDB.successPrefix + "Successfully generated the script at " + ChatColor.GOLD + resp.getLink());
			return true;
		} else {
			p.sendMessage(RSDB.errorPrefix + "Unable to generate the script.");
			return false;
			
		}
	}
	
	/*
	 * 	Parameters:
	 * 		String url - the url of the script
	 * 		Player p - the player who owns the script
	 * 
	 * 	Description:
	 * 		Fetches and returns the script or null if the script cannot be fetched
	 */
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
			p.sendMessage(RSDB.errorPrefix + "Failed to get script. Did you enter the paste.ee URL correctly?");
			return null;
		}
		
		p.sendMessage(RSDB.successPrefix + "Successfully fetched script at " + url + ".");
		
		Paste paste = resp.getPaste();
		Section section = paste.getSections().get(0);
		
		return section.getContents();
	}
}
