package com.gmail.redstonebunny.rsdb.script;

import java.util.HashMap;

import com.gmail.redstonebunny.rsdb.variables.Variable;

public class Parser {
	
	private static final String[][] operators = {
			{"!", "~"},
			{"*", "/", "%"},
			{"+", "-"},
			{"<<", ">>"},
			{"<", "<=", ">", ">="},
			{"==", "!=",},
			{"&"},
			{"^"},
			{"|"},
			{"&&"},
			{"||"},
			{"="}
	};
	
	private static final char[] operatorLastChars = {'<', '>', '=', '&', '|'};
	
	/*
	 *	Parameters:
	 *		String str - the string representation of a variable
	 *		HashMap<String, Variable> vars - the list of all variables
	 *
	 *	Return Value:
	 *		Returns the integer representation of the variable
	 *		Returns null if there was an error
	 */
	public static Integer getValue(String str, HashMap<String, Variable> vars) {
		if(!str.startsWith("$")) {
			Integer tmp;
			if((tmp = stringToInt(str)) != null)
				return tmp;
			return null;
		}
		
		str = str.substring(1);
		int b = str.indexOf('[');
		int e = str.lastIndexOf(']');
		
		Integer pos = -1;
		
		if(b != -1 && e != -1) {
			pos = evaluateExpression(str.substring(b + 1, e), vars);
			if(pos == null)
				return null;
			str = str.substring(0, b) + str.substring(e + 1);
		}
		
		Variable v = vars.get(str);
		if(v == null)
			return null;
		
		if(pos == -1)
			return v.getValue();
		else if(pos >= 0 && pos < v.getSize()) 
			return v.getPastValue(pos);
		else
			return null;
	}
	
	public static Integer evaluateExpression(String str, HashMap<String, Variable> vars) {
		str = str.replaceAll("\\s", "");
		int index_p1 = str.indexOf("(");
		while(index_p1 != -1) {
			int index_p2 = getFirstClosingPar(str, index_p1);
			if(index_p2 == -1)
				return null;
			Integer tmp = evaluateExpression(str.substring(index_p1 + 1, index_p2), vars);
			if(tmp == null)
				return null;
			str = str.replaceFirst(str.substring(index_p1, index_p2 + 1), Integer.toString(tmp));
			index_p1 = str.indexOf("(");
		}
		
		return evaluateExpressionRecurse(str, vars);
	}
	
	private static Integer evaluateExpressionRecurse(String str, HashMap<String, Variable> vars) {
		String op = "";
		int index = -1;
		
		for(int i = operators.length - 1; i >= 0; i--) {
			for(int j = 0; j < operators[i].length; j++) {
				int bracketCount = 0;
				for(int k = 0; k < str.length(); k++) {
					if(str.charAt(k) == '[')
						bracketCount++;
					if(str.charAt(k) == ']')
						bracketCount--;
					if(bracketCount == 0 && str.length() >= k + operators[i][j].length() && (str.substring(k, k+operators[i][j].length()).equals(operators[i][j])) && 
							!isOperatorLastChar(str.charAt(k + operators[i][j].length()))) {
						if(k > index) {
							op = operators[i][j];
							index = k;
						}
					}
				}
			}
			if(index > -1) {
				break;
			}
		}
		
		if(index == -1)
			return getValue(str, vars);
		
		Integer arg1 = evaluateExpressionRecurse(str.substring(0, index), vars);
		Integer arg2 = evaluateExpressionRecurse(str.substring(index + op.length()), vars);
		
		if(op.equals("!") || op.equals("~") || op.equals("=")) {
			if(arg2 == null)
				return null;
		}
		else if(arg1 == null || arg2 == null) {
			return null;
		}
		
		switch(op) {
		case("!") :
			return (arg2 == 0) ? 1 : 0;
		case("~") :
			return ~arg2;
		case("*") :
			return arg1 * arg2;
		case("/") :
			return arg1 / arg2;
		case("%") :
			return arg1 % arg2;
		case("+") :
			return arg1 + arg2;
		case("-") :
			return arg1 - arg2;
		case("<<") :
			return arg1 << arg2;
		case(">>") :
			return arg1 >> arg2;
		case(">") :
			return (arg1 > arg2) ? 1 : 0;
		case(">=") :
			return (arg1 >= arg2) ? 1 : 0;
		case("<") :
			return (arg1 < arg2) ? 1 : 0;
		case("<=") :
			return (arg1 <= arg2) ? 1 : 0;
		case("==") :
			return (arg1 == arg2) ? 1 : 0;
		case("!=") :
			return (arg1 != arg2) ? 1 : 0;
		case("&") :
			return arg1 & arg2;
		case("^") :
			return arg1 ^ arg2;
		case("|") :
			return arg1 | arg2;
		case("&&") :
			return (arg1 != 0 && arg2 != 0) ? 1 : 0;
		case("||") :
			return (arg1 != 0 || arg2 != 0) ? 1 : 0;
		case("=") :
			String var = str.substring(0, index);
			if(!var.startsWith("$")) {
				return null;
			} else if(var.charAt(1) == '#') {
				if(vars.containsKey(var.substring(1))) {
					vars.get(var.substring(1)).setValue(arg2);
				}
				else {
					return null;
				}
			} else {
				if(vars.containsKey(var.substring(1))) {
					vars.get(var.substring(1)).setValue(arg2);
				} else if(Variable.isLegalVarName(var.substring(1))) {
					vars.put(var.substring(1), new Variable(var.substring(1), vars.get("#PIPE_SIZE")));
				}
				else {
					return null;
				}
			}
			return arg2;
			
		default:
			return null;
		}
	}
	
	private static boolean isOperatorLastChar(char c) {
		for(int i = 0; i < operatorLastChars.length; i++) {
			if(c == operatorLastChars[i])
				return true;
		}
		
		return false;
	}
	
	private static int getFirstClosingPar(String str, int indexStart) {
		char[] chars = str.toCharArray();
		int count = 1;
		for(int i = indexStart + 1; i < chars.length; i++) {
			if(chars[i] == '(')
				count++;
			else if(chars[i] == ')')
				count--;
			
			if(count == 0)
				return i;
		}
		
		return -1;
	}
	
	public static Integer stringToInt(String str) {
		try {
			return Integer.parseInt(str);
		} catch(Exception e) {
			return null;
		}
	}
}
