package extractor;

import java.util.HashMap;


public class SPARQLBlockExtractor extends TextExtractor {

	public static final String [] SPARQL_KEYWORDS ={"GRAPH", "SELECT", "GROUP BY", "ORDER BY"};
	
	public SPARQLBlockExtractor(String whereClause) {
		super(whereClause);
	}
	
	public HashMap<Integer, String> extractOptionals(String var) {
		HashMap<Integer, String> optionalClauses = new HashMap<Integer, String>();		
		//look for all appearances of var
		//for each appearance look for the nearest OPTIONAL keyword and check that the OPTIONAL block contains the var
		
		int varIndex = 0;
		int lastIndexOfOptional = 0;
		int fromIndex = 0;
		
		while (true){
			varIndex = text.indexOf(var, fromIndex);			
			if (varIndex==-1){
				break;
			}
			
			lastIndexOfOptional = text.lastIndexOf("OPTIONAL", varIndex);
			if (lastIndexOfOptional!=-1){
				int lastClosingBracket = findClosingBracket(text.indexOf('{', lastIndexOfOptional));
				if (varIndex<lastClosingBracket){
					optionalClauses.put(lastIndexOfOptional, text.substring(lastIndexOfOptional, lastClosingBracket));
				}
				else{
					optionalClauses.put(-1, var);//shows there are triple patterns which are not OPTIONAL
					break;
				}
			}
			else{
				optionalClauses.put(-1, var);
				break;
			}
							
			fromIndex = varIndex+var.length()+1;
		}
		
		return optionalClauses;
	}

	public HashMap<String, String> extractGraphNamesAndOuterBlocks(String var) {
		HashMap<String, String> relevantSPARQLBlocks = new HashMap<String, String>();// map between the graph names and the relevant block within that graph in which the variable appears		
		//look for all appearances of var
		//for each appearance look for the nearest GRAPH keyword before its appearance
		
		int varIndex = 0;
		int lastIndexOfGraph = 0;
		int fromIndex = 0;
		
		while (true){
			varIndex = text.indexOf(var, fromIndex);	
			if (varIndex==-1){
				break;
			}
			
			if (currentLineContainsKeyword(text, varIndex)){
				fromIndex = varIndex+var.length()+1;
				continue;
			}		
			
			lastIndexOfGraph = text.lastIndexOf("GRAPH", varIndex);
			if (lastIndexOfGraph!=-1){
				int indexOfEndingGraphName = text.indexOf('>', lastIndexOfGraph);
				int lastClosingBracket = findClosingBracket(text.indexOf('{', indexOfEndingGraphName)+1);
				if (varIndex>lastClosingBracket){
					throw new RuntimeException("Assumption violated: required triple patterns should appear before OPTIONAL, UNION, GRAPH clauses");
				}
				else{
					int graphNameStart = text.indexOf('<', lastIndexOfGraph)+1;
					int graphNameEnd = text.indexOf('>', lastIndexOfGraph);
					String graphName = text.substring(graphNameStart, graphNameEnd);			
					
					//ASSUMPTION: if a variable appears multiple times in a block, show only the first extracted outer block
					if (!relevantSPARQLBlocks.containsKey(graphName)){
						String outerBlock = extractOuterBlock(varIndex);
						relevantSPARQLBlocks.put(graphName, outerBlock);
					}
				}
			}
			else{
				if (!relevantSPARQLBlocks.containsKey("DEFAULT")){
					relevantSPARQLBlocks.put("DEFAULT", extractDefaultGraph(text, var));
				}
			}
				
			fromIndex = varIndex+var.length()+1;
		}
		
		return relevantSPARQLBlocks;
	}
	
	private boolean currentLineContainsKeyword(String text, int varIndex) {
		int lastEOL = text.lastIndexOf('\n', varIndex);
		int nextEOL = text.indexOf('\n', varIndex);
		String currentLine = text.substring(lastEOL+1, nextEOL);
		
		for (int i = 0; i < SPARQL_KEYWORDS.length; i++) {
			if (currentLine.indexOf(SPARQL_KEYWORDS[i])!=-1)
				return true;
		}
		return false;
	}

	private String extractOuterBlock(int varIndex){
		int openBracket = text.lastIndexOf("{", varIndex);
		int bracketIndex=1;
		int charIndex=openBracket+1;
		while (bracketIndex!=0){
			if (text.charAt(charIndex)=='{'){
				bracketIndex++;
			}
			else if (text.charAt(charIndex)=='}'){
				bracketIndex--;
			}
			
			charIndex++;
		}
		
		return text.substring(openBracket, charIndex);
	}

	private int findClosingBracket(int fromIndex){
		int bracketNo = 1;
		int charIndex=fromIndex+1;
		
		while (bracketNo!=0){
			if (text.charAt(charIndex)=='{'){
				bracketNo++;
			}
			else if (text.charAt(charIndex)=='}'){
				bracketNo--;
			}
			
			charIndex++;
		}
		
		return charIndex;
	}
	
	private String extractDefaultGraph(String text, String var) {
		String keyword = "(UNION|OPTIONAL|GRAPH)";
		String defaultGraph = "(.*?)"+keyword;
		String pattern = extractFirstPattern(text, defaultGraph, 1);
		
		return pattern;
	}
}
