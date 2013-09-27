package generator;

import java.util.HashMap;

public class PrettyPrinter {
	
	public static void printCurrentLine(String line){
		System.out.println("Markup for the template line: "+line);
	}
	
	public static void printOPTIONALClauses(HashMap<Integer, String> optionalClauses, String var){
		System.out.println("Optionals Clauses in which the variable "+var+" appears: ");
		for (String optional : optionalClauses.values()) {
			System.out.println("-----------------------");
			System.out.println(optional);
		}
		System.out.println("=======================");
	}
	
	public static void printGraphNames(HashMap<String, String> graphBlocks, String var){
		System.out.println("Graphs in which the variable "+var+" appears: ");
		for (String graphName : graphBlocks.keySet()) {
			System.out.println(graphName);
		}
		System.out.println("=======================");
	}

	public static void printOuterBlocks(HashMap<String, String> outerBlocks, String var){
		System.out.println("Blocks in which the variable "+var+" appears: ");
		for (String outerBlock : outerBlocks.values()) {
			System.out.println("-----------------------");
			System.out.println(outerBlock);
		}
		System.out.println("=======================");
	}
	
	
}
