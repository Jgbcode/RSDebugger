package com.gmail.redstonebunny.rsdb.script;

import java.util.HashMap;

import org.bukkit.entity.Player;

import com.gmail.redstonebunny.rsdb.variables.Operator;
import com.gmail.redstonebunny.rsdb.variables.Variable;

/*
 * 	Parse class - contains static methods for parsing expressions
 */

public class Parser {
	
	// A list of all valid operators
	private static final String[][] operators = {
			{"!", "~"},
			{"*", "/", "%"},
			{"+", "-"},
			{"<<", ">>"},
			{"<", "<=", ">", ">="},
			{"==", "!=",},
			{"&", "~&"},
			{"^", "~^"},
			{"|", "~|"},
			{"&&"},
			{"||"},
			{"="}
	};
	
	// A list of all characters that may be the second character in an operator
	// Used to differentiate between operators such as "==" and "="
	private static final String[] operatorsTwo = {
			"<<", ">>", "<=", ">=", "==", "!=", "~&", "~^", "~|", "&&", "||"
	};
	
	// A hashmap of all operators for converting into integers
	private static final HashMap<String, Integer> operatorStrings;
	static {
		operatorStrings = new HashMap<String, Integer>();
		operatorStrings.put("!", 0);
		operatorStrings.put("~", 1);
		operatorStrings.put("*", 2);
		operatorStrings.put("/", 3);
		operatorStrings.put("%", 4);
		operatorStrings.put("+", 5);
		operatorStrings.put("-", 6);
		operatorStrings.put("<<", 7);
		operatorStrings.put(">>", 8);
		operatorStrings.put("<", 9);
		operatorStrings.put("<=", 10);
		operatorStrings.put(">", 11);
		operatorStrings.put(">=", 12);
		operatorStrings.put("==", 13);
		operatorStrings.put("!=", 14);
		operatorStrings.put("&", 15);
		operatorStrings.put("~&", 16);
		operatorStrings.put("^", 17);
		operatorStrings.put("~^", 18);
		operatorStrings.put("|", 19);
		operatorStrings.put("~|", 20);
		operatorStrings.put("&&", 21);
		operatorStrings.put("||", 22);
		operatorStrings.put("=", 23);
	}
	
	/*
	 *	Parameters:
	 *		String str - the string representation of a variable, the variable may have an expression
	 *			inside it's pipeline stage brackets.
	 *		HashMap<String, Variable> vars - the list of all variables
	 *
	 *	Returns:
	 *		Returns the integer representation of the variable
	 *		Returns null if there was an error
	 */
	public static Integer getValue(String str, HashMap<String, Variable> vars, Player p) {
		// DEBUG
		//System.out.println(str);
		
		if(str.length() == 0)
			return null;
		
		if(str.charAt(0) != '$') {
			return stringToInt(str);
		}
		
		str = str.substring(1);
		int b = str.indexOf('[');
		int e = str.lastIndexOf(']');
		
		Integer pos = -1;
		
		if(b != -1 && e != -1) {
			String index = str.substring(b + 1, e);
			str = str.substring(0, b) + str.substring(e + 1);
			if(index.equalsIgnoreCase("size")) {
				Variable v = vars.get(str);
				if(v == null)
					return null;
				else
					return v.getNumberOfBits();
			}
			else {
				pos = evaluateExpression(index, vars, p);
				if(pos == null)
					return null;
			}
		}
		
		//System.out.println(str + " : " + pos);
		
		Variable v = vars.get(str);
		if(v == null)
			return null;
		
		//System.out.println(v.getPastValue(pos));
		
		if(pos == -1)
			return v.getValue();
		else if(pos >= 0 && pos < v.getSize() + 1) 
			return v.getPastValue(pos);
		else
			return null;
	}
	
	/*
	 * 	Parameters:
	 * 		String str - an expression to be reduced to a single integer
	 * 		HashMap<String, Variable> vars - the list of all variables
	 * 
	 * 	Returns:
	 * 		An integer representing the value of the expression or null if the expression was unable to be parsed
	 */
	public static Integer evaluateExpression(String str, HashMap<String, Variable> vars, Player p) {
		// DEBUG
		//System.out.println(str);
		
		str = str.replaceAll("\\s", "");
		int index_a1 = str.indexOf('@');
		while(index_a1 != -1) {
			int index_a2 = str.indexOf('@', index_a1 + 1);
			if(index_a2 == -1)
				return null;
			Variable v = vars.get(str.substring(index_a1, index_a2 + 1));
			if(v instanceof Operator) {
				str = str.replaceFirst(str.substring(index_a1, index_a2 + 1), ((Operator)v).getOperator());
			}
			else {
				return null;
			}
			index_a1 = str.indexOf('@');
		}
		
		int index_p1 = str.indexOf("(");
		while(index_p1 != -1) {
			int index_p2 = getFirstClosingPar(str, index_p1);
			if(index_p2 == -1)
				return null;
			Integer tmp = evaluateExpression(str.substring(index_p1 + 1, index_p2), vars, p);
			if(tmp == null)
				return null;
			str = str.substring(0, index_p1) + tmp + str.substring(index_p2 + 1);
			index_p1 = str.indexOf("(");
		}
		
		return evaluateExpressionRecurse(str, vars, p);
	}
	
	/*
	 * 	Parameters:
	 * 		String str - an expression to be reduced to a single integer, the expression cannot have parenthesis
	 * 		HashMap<String, Variable> vars - the list of all variables
	 * 
	 * 	Returns:
	 * 		An integer representing the value of the expression or null if the expression was unable to be parsed
	 */
	private static Integer evaluateExpressionRecurse(String str, HashMap<String, Variable> vars, Player p) {
		Integer val = getValue(str, vars, p);
		if(val != null)
			return val;
		
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
					if(bracketCount == 0 && str.length() >= k + operators[i][j].length() && (str.substring(k, k+operators[i][j].length()).equals(operators[i][j]))) {
						if(operators[i][j].length() == 2 || isOperatorSingle(str, k)) {
							if(k > index) {
								op = operators[i][j];
								index = k;
							}
						} 
					}
				}
			}
			if(index > -1) {
				break;
			}
		}
		
		if(index == -1)
			return null;
		
		// DEBUG:
		//System.out.println("String: " + str);
		//System.out.println("Op: " + op);
		
		Integer arg1 = evaluateExpressionRecurse(str.substring(0, index), vars, p);
		Integer arg2 = evaluateExpressionRecurse(str.substring(index + op.length()), vars, p);
		
		// DEBUG:
		//System.out.println("Arg1: " + str.substring(0, index) + " = " + arg1);
		//System.out.println("Arg2: " + str.substring(index + op.length()) + " = " + arg2);
		
		if(op.equals("!") || op.equals("~") || op.equals("=") || op.equals('-')) {
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
			if(arg1 == null)
				return -arg2;
			else
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
		case("~&") :
			return ~(arg1 & arg2);
		case("^") :
			return arg1 ^ arg2;
		case("~^") :
			return ~(arg1 ^ arg2);
		case("|") :
			return arg1 | arg2;
		case("~|") :
			return ~(arg1 | arg2);
		case("&&") :
			return (arg1 != 0 && arg2 != 0) ? 1 : 0;
		case("||") :
			return (arg1 != 0 || arg2 != 0) ? 1 : 0;
		case("=") :
			String var = str.substring(0, index);
			if(var.startsWith("$")) {
				if(var.charAt(1) == '#') {
					if(vars.get(var.substring(1)) != null) {
						vars.get(var.substring(1)).setValue(arg2);
					}
					else {
						return null;
					}
				} else {
					if(vars.get(var.substring(1)) != null) {
						vars.get(var.substring(1)).setValue(arg2);
					} else if(Variable.isLegalVarName(var.substring(1))) {
						Variable tmp = new Variable(var.substring(1), vars.get("#PIPE_SIZE"), p);
						tmp.setValue(arg2);
						vars.put(var.substring(1), tmp);
					}
					else {
						return null;
					}
				}
			} else if(var.startsWith("@")) {
				if(vars.get(var) == null) {
					Operator o = Operator.createOperator(var, vars.get("#PIPE_SIZE"), p, 0);
					if(o == null)
						return null;
					o.setValue(arg2);
					vars.put(var, o);
				}
				else {
					vars.get(var).setValue(arg2);
				}
			}
			else {
				return null;
			}
			
			return arg2;
		default:
			return null;
		}
	}
	
	/*
	 * 	Parameter:
	 * 		char c - the character to be tested
	 * 
	 * 	Returns:
	 * 		True if the character is in operatorLastChars
	 */
	private static boolean isOperatorSingle(String str, int index) {
		if(index > 0) {
			for(String s : operatorsTwo) {
				if(s.equals(str.substring(index-1, index+1)))
					return false;
			}
		}
		
		if(index < str.length() - 1) {
			for(String s : operatorsTwo) {
				if(s.equals(str.substring(index, index+2)))
					return false;
			}
		}
		
		return true;
	}
	
	/*
	 * 	Parameters:
	 * 		String str - the string to find the index of the closing parenthesis
	 * 		int indexStart - the index of the opening parenthesis
	 * 
	 * 	Returns:
	 * 		The index of the closing parenthesis or -1 if the closing parenthesis could not be found
	 */
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
	
	/*
	 * 	Parameters:
	 * 		String str - the string to be converted into an integer
	 * 
	 * 	Returns:
	 * 		The integer representation of the string or null if the string could not be parsed
	 */
	public static Integer stringToInt(String str) {
		if(operatorStrings.containsKey(str))
			return operatorStrings.get(str);
		
		try {
			return Integer.parseInt(str);
		} catch(Exception e) {
			return null;
		}
	}
}
