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
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import com.gmail.redstonebunny.rsdb.RSDB;
import com.gmail.redstonebunny.rsdb.variables.Clock;
import com.gmail.redstonebunny.rsdb.variables.Reset;
import com.gmail.redstonebunny.rsdb.variables.Variable;
import com.gmail.redstonebunny.rsdb.variables.Watchpoint;

/*
 * 	Script class - holds and executes a script
 */

public class Script {
	private RSDB rsdb;
	private Document xml;
	private Player p;
	private HashMap<String, Variable> vars;
	
	/*
	 * 	Parameters:
	 * 		String url - the url to a pastee which holds the script to be loaded
	 * 		Player p - the player who is loading this script
	 * 
	 * 	Returns:
	 * 		A script object or null if the script could not be loaded
	 */
	public static Script createScript(String url, Player p, HashMap<String, Variable> vars, RSDB rsdb) {
		String str = ScriptInterface.getScript(url, p);
		if(str == null)
			return null;
		
		Document d = parseXML(str, p);
		if(d == null)
			return null;
		
		Script s = new Script(d, p, vars, rsdb);
		
		if(s.executeScriptSection("init"))
			return s;
		return null;
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
	
	public Script(Player p, HashMap<String, Variable> vars, RSDB rsdb) {
		this.xml = null;
		this.p = p;
		this.vars = vars;
		this.rsdb = rsdb;
	}
	
	private Script(Document xml, Player p, HashMap<String, Variable> vars, RSDB rsdb) {
		this.xml = xml;
		this.p = p;
		this.vars = vars;
		this.rsdb = rsdb;
	}
	
	public boolean genScript() {
		String script = "<rsdb>\n\t<init>\n";
		boolean b = false;
		for(Variable v : vars.values()) {
			if(v instanceof Watchpoint) {
				script += "\t\t<watch name=\"" + v.getName() + "\">\n";
				for(Location l : v.getLocation())
					script += genLocation(l);
				script += "\t\t</watch>\n";
				b = true;
			} else if(v instanceof Clock) {
				script += "\t\t<clock>\n";
				for(Location l : v.getLocation())
					script += genLocation(l);
				script += "\t\t</clock>\n";
				b = true;
			} else if(v instanceof Reset) {
				script += "\t\t<reset>\n";
				for(Location l : v.getLocation())
					script += genLocation(l);
				script += "\t\t</reset>\n";
				b = true;
			}
		}
		if(!b) {
			script += "\t\t<!-- ENTER INITIATION CODE HERE -->\n";
		}
		script += "\t</init>\n";
		script += "\t<loop>\n";
		script += "\t\t<!-- ENTER LOOP CODE HERE -->\n";
		script += "\t</loop>\n";
		script += "\t<restart>\n";
		script += "\t\t<!-- ENTER RESET CODE HERE -->\n";
		script += "\t</restart>\n";
		script += "</rsdb>";
		
		return ScriptInterface.sendScript(script, p);
	}
	
	private String genLocation(Location l) {
		return "\t\t\t<location world=\"" + l.getWorld().getUID() + "\" x=\"" + l.getBlockX() + "\" y=\"" + l.getBlockY() + "\" z=\"" + l.getBlockZ() + "\"/>\n";
	}
	
	public boolean executeScriptSection(String name) {
		NodeList nodes = xml.getDocumentElement().getElementsByTagName(name);
		if(nodes.getLength() == 1) {
			return executeScriptSection(nodes.item(0));
		}
		return true;
	}
	
	public boolean executeScriptSection(Node n) {
		if(n == null)
			return true;
		
		NodeList list = n.getChildNodes();
		for(int i = 0; i < list.getLength(); i++) {
			Node tmp = list.item(i);
			
			if(!(tmp instanceof Element))
				continue;
			
			Method method;
			System.out.println("element" + tmp.getNodeName());
			try {
				method = this.getClass().getDeclaredMethod("element" + tmp.getNodeName(), Node.class);
			} catch(Exception e) {
				e.printStackTrace();
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
	private boolean elementreset(Node n) {
		if(n.getAttributes().getLength() == 0) {
			if(n.getChildNodes().getLength() == 1 && n.getChildNodes().item(0).getNodeName().equals("location")) {
				Location l = elementlocation(n.getFirstChild());
				if(l == null)
					return false;
				Reset c = Reset.createReset(rsdb, p, vars.get("#PIPE_SIZE"), l, this);
				if(c == null)
					return false;
				vars.put("#CLOCK", c);
				return true;
			}
			else {
				scriptError(n, "Clocks can only have a single location as their child element.");
				return false;
			}
		}
		else {
			scriptError(n, "Illegal clock attributes.");
			return false;
		}
	}
	
	@SuppressWarnings("unused")
	private boolean elementclock(Node n) {
		if(n.getAttributes().getLength() == 0) {
			if(n.getChildNodes().getLength() == 1 && n.getChildNodes().item(0).getNodeName().equals("location")) {
				Location l = elementlocation(n.getFirstChild());
				if(l == null)
					return false;
				Clock c = Clock.createClock(rsdb, p, vars.get("#PIPE_SIZE"), vars, this, l);
				if(c == null)
					return false;
				vars.put("#CLOCK", c);
				return true;
			}
			else {
				scriptError(n, "Clocks can only have a single location as their child element.");
				return false;
			}
		}
		else {
			scriptError(n, "Illegal clock attributes.");
			return false;
		}
	}
	
	@SuppressWarnings("unused")
	private boolean elementif(Node n) {
		if(n.getAttributes().getLength() == 1 && n.getAttributes().item(0).getNodeName().equals("statement")) {
			String statement = n.getAttributes().item(0).getNodeValue();
			Integer result = Parser.evaluateExpression(statement, vars, p);
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
				if(!(n.getChildNodes().item(i) instanceof Element))
					continue;
				
				if(n.getChildNodes().item(i).getNodeName().equals("location")) {
					Location l = elementlocation(n.getChildNodes().item(i));
					if(l == null)
						return false;
					locs.add(l);
				}
				else {
					scriptError(n.getChildNodes().item(i), "All child elements of watch must be of type location.");
					return false;
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
					System.out.println(world);
					System.out.println(p.getWorld().getUID());
					if(world.equals(p.getWorld().getUID().toString())) {
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
		p.sendMessage(RSDB.errorPrefix + "Script runtime error at " + n.getNodeName().replaceAll("\\s", "") + ". Error: " + error);
		String stack = "";
		Node tmp = n;
		while(tmp != null) {
			stack += tmp.getNodeName() + " <- ";
			tmp = tmp.getParentNode();
		}
		if(stack.length() == 0)
			stack = "null";
		else
			stack = stack.substring(0, stack.length() - 4);
		
		p.sendMessage(RSDB.errorPrefix + "Node stack: " + stack);
	}
}
