package com.gmail.redstonebunny.rsdb.variables;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import com.gmail.redstonebunny.rsdb.RSDB;

public class Operator extends Variable {

	private static final String[] operators = {"!", "~", "*", "/", "%", "+", "-", "<<", ">>", "<", "<=", ">",
			">=", "==", "!=", "&", "~&", "^", "~^", "|", "~|", "&&", "||", "=" };
	
	public static Operator createOperator(String name, Variable size, Player p, int value) {
		if(name.length() > 2 && name.startsWith("@") && name.endsWith("@")) {
			if(Variable.isLegalVarName(name.substring(1, name.length() - 1))) {
				if(value >= 0 && value < operators.length) {
					return new Operator(name, size, p, value);
				}
				else {
					p.sendMessage(RSDB.prefix + "Illegal value for operator variable.");
					return null;
				}
			}
			else {
				p.sendMessage(RSDB.prefix + "Illegal variable name.");
				return null;
			}
		}
		else {
			p.sendMessage(RSDB.prefix + "Operator variables must start and end with '@'.");
			return null;
		}
	}
	
	public Operator(String name, Variable size, Player p, int value) {
		super(name, size, p);
		currentValue = value;
	}
	
	public String getOperator() {
		if(super.getValue() >= 0 && super.getValue() < operators.length)
			return operators[super.getValue()];
		return null;
	}

	@Override
	public void printValue() {
		p.sendMessage(RSDB.prefix + ChatColor.GOLD + name + ChatColor.GRAY + " = " + operators[getValue()]);
	}
	
	@Override
	public void printLocation() {
		p.sendMessage(RSDB.prefix + ChatColor.GOLD + name + ChatColor.GRAY + " = " + operators[getValue()]);
	}
	
	@Override
	public void setValue(int value) {
		if(value >= 0 && value < operators.length)
			currentValue = value;
		else
			p.sendMessage(RSDB.errorPrefix + "Illegal value set for " + name + ".");
	}
}
