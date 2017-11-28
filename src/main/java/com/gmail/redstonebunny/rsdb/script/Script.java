package com.gmail.redstonebunny.rsdb.script;

import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.lang.StringEscapeUtils;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import com.gmail.redstonebunny.rsdb.RSDB;
import com.gmail.redstonebunny.rsdb.variables.Clock;
import com.gmail.redstonebunny.rsdb.variables.Operator;
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
	private World world;
	
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
		
		str = escapeXML(str);
		
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
	
	private static String escapeXML(String xml) {
		int index = xml.indexOf("{");
		while(index != -1) {
			if(index == 0 || xml.charAt(index) != '\\') {
				int end_index = xml.indexOf("}", index + 1);
				while(xml.charAt(end_index) == '\\')
					end_index = xml.indexOf('}', end_index + 1);
				// DEBUG
				//System.out.println(xml.substring(index + 1, end_index));
				
				xml = xml.substring(0, index) + StringEscapeUtils.escapeXml(xml.substring(index + 1, end_index)) + xml.substring(end_index + 1);
				index = xml.indexOf("{", end_index + 1);
			}
			else {
				index = xml.indexOf("{", index + 1);
			}
		}
		// DEBUG
		//System.out.println(xml);
		return xml;
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
		int count = 1;
		script += "\t\t<world uid=\"" + p.getWorld().getUID() + "\" id=\"0\"/>\n";
		for(Variable v : vars.values()) {
			if(v instanceof Watchpoint) {
				script += "\t\t<watch name=\"" + v.getName() + "\" id=\"" + count + "\">\n";
				for(Location l : v.getLocation())
					script += genLocation(l);
				script += "\t\t</watch>\n";
				b = true;
				for(Location l : v.getLocation()) {
					if(!l.getWorld().equals(p.getWorld())) {
						p.sendMessage(RSDB.errorPrefix + "You must be in the same world as all your watchpoints.");
					}
				}
				count++;
			} else if(v instanceof Clock) {
				script += "\t\t<clock id=\"" + count + "\">\n";
				for(Location l : v.getLocation())
					script += genLocation(l);
				script += "\t\t</clock>\n";
				b = true;
				for(Location l : v.getLocation()) {
					if(!l.getWorld().equals(p.getWorld())) {
						p.sendMessage(RSDB.errorPrefix + "You must be in the same world as your clock.");
					}
				}
				count++;
			} else if(v instanceof Reset) {
				script += "\t\t<reset id=\"" + count + "\">\n";
				for(Location l : v.getLocation())
					script += genLocation(l);
				script += "\t\t</reset>\n";
				b = true;
				for(Location l : v.getLocation()) {
					if(!l.getWorld().equals(p.getWorld())) {
						p.sendMessage(RSDB.errorPrefix + "You must be in the same world as your reset.");
					}
				}
				count++;
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
		return "\t\t\t<location x=\"" + l.getBlockX() + "\" y=\"" + l.getBlockY() + "\" z=\"" + l.getBlockZ() + "\"/>\n";
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
				e.printStackTrace();
				scriptError(tmp, "Unable to process element \"" + tmp.getNodeName() + "\"");
				return false;
			}
		}
		
		return true;
	}
	
	@SuppressWarnings("unused")
	private boolean elementassert(Node n) {
		if(n.getChildNodes().getLength() == 1 && n.getChildNodes().item(0) instanceof Text) {
			String text = n.getChildNodes().item(0).getTextContent();
			Integer result = Parser.evaluateExpression(text, vars, p);
			if(result == null) {
				scriptError(n, "Could not parse expression.");
				return false;
			} else if(result == 0) {
				p.sendMessage(RSDB.prefix + "Assertion failed: id=" + 
						((n.getAttributes().getNamedItem("id") == null) ? "null" : n.getAttributes().getNamedItem("id").getTextContent()) + 
						"\n" + RSDB.prefix + "Assertion: " + text + " evaluated to " + result);
				return false;
			}
			return true;
		}
		else {
			scriptError(n, "Illegal assertion definition.");
			return false;
		}
	}
	
	@SuppressWarnings("unused")
	private boolean elementdelete(Node n) {
		if(removeText(n)) {
			p.sendMessage(RSDB.prefix + "Warning: Found text inside reset declaration. The text is being removed.");
		}
		
		if(n.getChildNodes().getLength() == 0) {
			if(n.getAttributes().getLength() > 0 && n.getAttributes().getNamedItem("name") != null) {
				if(vars.get(n.getAttributes().getNamedItem("name").getTextContent()) == null)
				{
					scriptError(n, "No variable named \"" + n.getAttributes().getNamedItem("name").getTextContent() + "\".");
					return false;
				}
				
				if(!n.getAttributes().getNamedItem("name").getTextContent().equals("#PIPE_SIZE")) {
					vars.put(n.getAttributes().getNamedItem("name").getTextContent(), null);
				}
				else {
					scriptError(n, "Attempting to delete #PIPE_SIZE.");
					return false;
				}
			}
			else {
				scriptError(n, "Delete is lacking attributes.");
				return false;
			}
		}
		else {
			scriptError(n, "Delete cannot have child elements.");
			return false;
		}
		
		return true;
	}
	
	@SuppressWarnings("unused")
	private boolean elementset(Node n) {
		if(removeText(n)) {
			p.sendMessage(RSDB.prefix + "Warning: Found text inside set declaration. The text is being removed.");
		}
		
		if(n.getChildNodes().getLength() == 0) {
			if(n.getAttributes().getLength() >= 2 && n.getAttributes().getNamedItem("name") != null && n.getAttributes().getNamedItem("to") != null) {
				String name = n.getAttributes().getNamedItem("name").getTextContent();
				Integer from = Parser.getValue(n.getAttributes().getNamedItem("to").getTextContent(), vars, p);
				if(from == null) {
					from = Parser.evaluateExpression(n.getAttributes().getNamedItem("to").getTextContent(), vars, p);
					if(from == null) {
						scriptError(n, "Unable to evaluate from expression.");
						return false;
					}
				}
				
				if(vars.get(name) == null) {
					if(name.startsWith("@")) {
						Operator o = Operator.createOperator(name, vars.get("#PIPE_SIZE"), p, 0);
						if(o == null) {
							scriptError(n, "Error creating operator.");
							return false;
						}
						
						o.setValue(from);
						vars.put(name, o);
					}
					else if(Variable.isLegalVarName(name)) {
						Variable v = new Variable(name, vars.get("#PIPE_SIZE"), p);
						v.setValue(from);
						vars.put(name, v);
					}
					else {
						scriptError(n, "Illegal variable name \"" + name + "\".");
						return false;
					}
				}
				else {
					vars.get(name).setValue(from);
				}
			}
			else {
				scriptError(n, "Illegal set attributes.");
				return false;
			}
		}
		else {
			scriptError(n, "Set elements cannot contain child nodes.");
			return false;
		}
		
		return true;
	}
	
	@SuppressWarnings("unused")
	private boolean elementmap(Node n) {
		if(n.getAttributes().getLength() >= 2 && n.getAttributes().getNamedItem("to") != null && n.getAttributes().getNamedItem("name") != null) {
			Integer var1 = Parser.evaluateExpression(n.getAttributes().getNamedItem("to").getTextContent(), vars, p);
			String var2 = n.getAttributes().getNamedItem("name").getTextContent();
			
			Variable var;
			
			if(vars.get(var2) == null) {
				if(var2.startsWith("@")) {
					var = Operator.createOperator(var2, vars.get("#PIPE_SIZE"), p, 0);
					if(var == null) {
						scriptError(n, "Illegal variable operator.");
						return false;
					}
				}
				else {
					var = new Variable(var2, vars.get("#PIPE_SIZE"), p);
				}
				
				vars.put(var2, var);
			}
			else {
				var = vars.get(var2);
			}
			
			if(n.getChildNodes().getLength() == 1 && n.getFirstChild() instanceof Text) {
				String text = n.getFirstChild().getTextContent();
				String[] cases = text.split(";");
				for(String s : cases) {
					String[] pair = s.split(":");
					if(pair.length != 2) {
						scriptError(n, "Illegal map pair: " + s);
						return false;
					}
					
					Integer cmp = Parser.evaluateExpression(pair[0], vars, p);
					if(cmp == null) {
						scriptError(n, "Unable to evaluate map expression: " + s);
						return false;
					} else if(cmp == var1) {
						Integer result = Parser.evaluateExpression(pair[1], vars, p);
						if(result == null) {
							scriptError(n, "Unable to evaluate map expression: " + s);
							return false;
						}
						
						var.setValue(result);
						return true;
					}
				}
			}
			else {
				scriptError(n, "Illegal elements contained within map.");
				return false;
			}
		}
		else {
			scriptError(n, "Illegal map attributes.");
			return false;
		}
		
		return true;
	}
	
	@SuppressWarnings("unused")
	private boolean elementscript(Node n) {
		if(n.getChildNodes().getLength() == 1 && n.getChildNodes().item(0) instanceof Text) {
			String script = n.getChildNodes().item(0).getTextContent().replaceAll("\\s", "");
			String[] exprs = script.split(";");
			for(String expr : exprs) {
				if(Parser.evaluateExpression(expr, vars, p) == null) {
					scriptError(n, "Failed to evaluate: " + expr);
					return false;
				}
			}
		}
		else {
			scriptError(n, "Illegal elements contained within script.");
			return false;
		}
		
		return true;
	}
	
	@SuppressWarnings("unused")
	private boolean elementreset(Node n) {
		if(removeText(n)) {
			p.sendMessage(RSDB.prefix + "Warning: Found text inside reset declaration. The text is being removed.");
		}
		
		if(n.getChildNodes().getLength() == 1 && n.getChildNodes().item(0).getNodeName().equals("location")) {
			Location l = elementlocation(n.getFirstChild());
			if(l == null)
				return false;
			Reset c = Reset.createReset(rsdb, p, vars.get("#PIPE_SIZE"), l, this);
			if(c == null)
				return false;
			vars.put("#RESET", c);
			return true;
		}
		else {
			scriptError(n, "Clocks can only have a single location as their child element.");
			return false;
		}
	}
	
	@SuppressWarnings("unused")
	private boolean elementclock(Node n) {
		if(removeText(n)) {
			p.sendMessage(RSDB.prefix + "Warning: Found text inside clock declaration. The text is being removed.");
		}
		
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
	
	@SuppressWarnings("unused")
	private boolean elementif(Node n) {
		if(n.getAttributes().getLength() > 0 && n.getAttributes().getNamedItem("expr") != null) {
			String statement = n.getAttributes().getNamedItem("expr").getTextContent();
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
		if(removeText(n)) {
			p.sendMessage(RSDB.prefix + "Found text element on watchpoint: " + n);
			p.sendMessage(RSDB.prefix + "The text has been removed.");
		}
		
		if(n.getAttributes().getLength() > 0 && n.getAttributes().getNamedItem("name") != null) {
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
		if(removeText(n)) {
			p.sendMessage(RSDB.prefix + "Found text element on location: " + n);
			p.sendMessage(RSDB.prefix + "The text has been removed.");
		}
		
		if(n.getChildNodes().getLength() == 0) {
			if(n.getAttributes().getLength() >= 3) {
				Integer x = Parser.stringToInt(n.getAttributes().getNamedItem("x").getNodeValue());
				Integer y = Parser.stringToInt(n.getAttributes().getNamedItem("y").getNodeValue());
				Integer z = Parser.stringToInt(n.getAttributes().getNamedItem("z").getNodeValue());
				
				if(world == null) {
					p.sendMessage(RSDB.errorPrefix + "World is null. You must set the world on the first line of the init segment.");
					return null;
				}
				
				if(x != null && y != null && z != null) {
					if(world.equals(p.getWorld())) {
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
	
	@SuppressWarnings("unused")
	private boolean elementworld(Node n) {
		if(removeText(n)) {
			p.sendMessage(RSDB.prefix + "Found text element on world: " + n);
			p.sendMessage(RSDB.prefix + "The text has been removed.");
		}
		
		if(n.getChildNodes().getLength() == 0) {
			if(n.getAttributes().getLength() > 0 && n.getAttributes().getNamedItem("uid") != null) {
				if(n.getAttributes().getNamedItem("uid").getTextContent().equals(p.getWorld().getUID().toString())) {
					world = p.getWorld();
				}
				else {
					scriptError(n, "The player is not in the world specified by the script.");
					return false;
				}
			}
			else {
				scriptError(n, "Illegal world attributes.");
				return false;
			}
		}
		else {
			scriptError(n, "Worlds cannot have child elements.");
			return false;
		}
		
		return true;
	}
	
	private boolean removeText(Node n) {
		boolean found = false;
		for(int i = 0; i < n.getChildNodes().getLength();) {
			if(n.getChildNodes().item(i) instanceof Text) {
				found = found || ((Text)n.getChildNodes().item(i)).getTextContent().replaceAll("\\s", "").length() > 0;
				n.removeChild(n.getChildNodes().item(i));
			}
			else {
				i++;
			}
		}
		
		return found;
	}
	
	private void scriptError(Node n, String error) {
		if(n.getAttributes().getNamedItem("id") == null) {
			p.sendMessage(RSDB.errorPrefix + "Script runtime error at " + n.getNodeName().replaceAll("\\s", "") + " id=null" + 
				". Error: " + error);
		}
		else {
			p.sendMessage(RSDB.errorPrefix + "Script runtime error at " + n.getNodeName().replaceAll("\\s", "") + " id=" + 
				n.getAttributes().getNamedItem("id").getNodeValue() + ". Error: " + error);
		}
	}
}
