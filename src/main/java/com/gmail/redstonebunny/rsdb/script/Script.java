package com.gmail.redstonebunny.rsdb.script;

import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import com.gmail.redstonebunny.rsdb.RSDB;
import com.gmail.redstonebunny.rsdb.variables.Variable;
import com.gmail.redstonebunny.rsdb.variables.Watchpoint;

/*
 * 	Script class - holds and executes a script
 */

public class Script {
	Document xml;
	Player p;
	HashMap<String, Variable> vars;
	
	/*
	 * 	Parameters:
	 * 		String url - the url to a pastee which holds the script to be loaded
	 * 		Player p - the player who is loading this script
	 * 
	 * 	Returns:
	 * 		A script object or null if the script could not be loaded
	 */
	public static Script createScript(String url, Player p, HashMap<String, Variable> vars) {
		String str = ScriptInterface.getScript(url, p);
		if(str == null)
			return null;
		
		Document d = parseXML(str, p);
		if(d == null)
			return null;
		
		return new Script(d, p, vars);
	}
	
	private static Document parseXML(String xml, Player p) {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = null;
		try {
			db = dbf.newDocumentBuilder();
			InputSource is = new InputSource();
			is.setCharacterStream(new StringReader(xml));
			try {
				Document doc = db.parse(is);
				return doc;
			} catch (SAXException e) {
				if(e instanceof SAXParseException) {
					SAXParseException pe = (SAXParseException) e;
					p.sendMessage(RSDB.errorPrefix + "Script parse error. Line: " + ((pe.getLineNumber() < 0) ? "unknown" : pe.getLineNumber()) + 
							" Column: " + ((pe.getColumnNumber() < 0) ? "unknown" : pe.getColumnNumber()) + " Error: " + 
							((pe.getMessage().length() < 0) ? "unknown" : pe.getMessage()));
				} else {
					p.sendMessage(RSDB.errorPrefix + "Script parse error.");
				}
				return null;
			} catch (IOException e) {
				p.sendMessage(RSDB.errorPrefix + "Script loading error. " + e.getMessage());
				return null;
			}
		} catch (ParserConfigurationException e1) {
			p.sendMessage(RSDB.errorPrefix + "Script configuration error.");
			return null;
		}
	}
	
	public Script(Document xml, Player p, HashMap<String, Variable> vars) {
		this.xml = xml;
		this.p = p;
		this.vars = vars;
	}
	
	public boolean executeSciptSection(String name) {
		NodeList nodes = xml.getDocumentElement().getElementsByTagName(name);
		if(nodes.getLength() == 1) {
			return executeScriptSection(nodes.item(1));
		}
		return true;
	}
	
	public boolean executeScriptSection(Node n) {
		NodeList list = n.getChildNodes();
		for(int i = 0; i < list.getLength(); i++) {
			Node tmp = list.item(i);
			Method method;
			try {
				method = this.getClass().getMethod("element" + tmp.getNodeName(), Node.class);
			} catch(Exception e) {
				scriptError(tmp, "No such element \"" + tmp.getNodeName() + "\"");
				return false;
			}
			
			try {
				Object result = method.invoke(this, tmp);
				if(result instanceof Boolean) {
					if(!(Boolean)result) {
						return false;
					}
				}
				else {
					scriptError(n, "Illegal return value.");
				}
			} catch(Exception e) {
				scriptError(tmp, "Unable to process element \"" + tmp.getNodeName() + "\"");
				return false;
			}
		}
		
		return true;
	}
	
	@SuppressWarnings("unused")
	private boolean elementif(Node n) {
		if(n.getAttributes().getLength() == 1 && n.getAttributes().item(0).getNodeName().equals("statement")) {
			String statement = n.getAttributes().item(0).getNodeValue();
			Integer result = Parser.evaluateExpression(statement, vars);
			if(result == null) {
				scriptError(n, "Unable to evaluate statement");
				return false;
			} else if(result == 0) {
				return true;
			}
			else {
				return executeScriptSection(n);
			}
		}
		else {
			scriptError(n, "Illegal if statement attributes.");
			return false;
		}
	}
	
	@SuppressWarnings("unused")
	private boolean elementwatch(Node n) {
		if(n.getAttributes().getLength() == 1 && n.getAttributes().item(0).getNodeName().equals("name")) {
			ArrayList<Location> locs = new ArrayList<Location>();
			for(int i = 0; i < n.getChildNodes().getLength(); i++) {
				if(n.getChildNodes().item(i).getNodeName().equals("location")) {
					Location l = elementlocation(n.getChildNodes().item(i));
					if(l == null)
						return false;
					locs.add(l);
				}
				else {
					scriptError(n.getChildNodes().item(i), "All child elements of watch must be of type location.");
				}
			}
			
			Watchpoint w = Watchpoint.createWatchpoint(n.getAttributes().getNamedItem("name").getNodeValue(), p, vars.get("#PIPE_SIZE"), locs);
			if(w == null)
				return false;
			vars.put(w.getName(), w);
			return true;
		}
		else {
			scriptError(n, "Illegal watchpoint attributes.");
			return false;
		}
	}
	
	private Location elementlocation(Node n) {
		if(n.getChildNodes().getLength() == 0) {
			if(n.getAttributes().getLength() == 4) {
				String world = n.getAttributes().getNamedItem("world").getNodeValue();
				Integer x = Parser.stringToInt(n.getAttributes().getNamedItem("x").getNodeValue());
				Integer y = Parser.stringToInt(n.getAttributes().getNamedItem("y").getNodeValue());
				Integer z = Parser.stringToInt(n.getAttributes().getNamedItem("z").getNodeValue());
				if(world != null && x != null && y != null && z != null) {
					if(world.equals(p.getWorld().getUID())) {
						return new Location(p.getWorld(), x, y, z);
					} else {
						scriptError(n, "Attempted to create a location in a world the player is not in.");
						return null;
					}
				} else {
					scriptError(n, "Illegal location attributes.");
					return null;
				}
			}
			else {
				scriptError(n, "Location is lacking attributes.");
				return null;
			}
		}
		else {
			scriptError(n, "Locations cannot have child elements.");
			return null;
		}
	}
	
	private void scriptError(Node n, String error) {
		p.sendMessage(RSDB.errorPrefix + "Script runtime error. Line: " + ((n.getUserData("linenumber") == null) ? "unknown" : n.getUserData("linenumber")) + 
				" Error: " + error);
	}
}
