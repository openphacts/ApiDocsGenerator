package extractor;

import java.util.HashMap;


public class SPARQLBlockExtractor extends TextExtractor {

	public static final String TRIPLE_PATTERN = "\\s*((\\?\\w*)|(<[^\\>]*>)|(\\w*:\\w*))?\\s*" +
															"(a|(\\?\\w*)|(\\w*:\\w*))\\s*" +
															"((\\?\\w*)|(<[^\\>]*>)|(\\w*:\\w*)|('[^']*'))\\s*";
	public static final String [] SPARQL_KEYWORDS ={"GRAPH", "SELECT", "GROUP BY", "ORDER BY"};
	
	public SPARQLBlockExtractor(String whereClause) {
		super(whereClause);
	}
	
	public String extractObjectFromTriple(String currentLine) {
		String lastToken = extractLastGroupPattern(currentLine,  TRIPLE_PATTERN);
		
		if (lastToken==null){
			throw new RuntimeException("Template triple not in expected format");
		}	
		
		//variable or graph name
		return lastToken;
	}
	
	public HashMap<String, String> extractGraphNamesAndOuterBlocksEfficiently(String var) {
		HashMap<String, String> relevantSPARQLBlocks = new HashMap<String, String>();// map between the graph names and the relevant block within that graph in which the variable appears		
		//look for all appearances of var
		//for each appearance look for the nearest GRAPH keyword before its appearance
		
		int varIndex = 0;
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
			
			findOuterGraph(relevantSPARQLBlocks, varIndex);
			fromIndex = varIndex+var.length()+1;
		}
		
		return relevantSPARQLBlocks;
	}
	
	private void findOuterGraph(HashMap<String, String> relevantSPARQLBlocks, int varIndex){
		int charIndex = varIndex;
		int bracketIndex = 0;
		int maxBracketIndex = 0;
		while (true){
			if (charIndex==0){
				String currentLine = getCurrentLine(text, charIndex);
				if (addNewGraphBlockMapping(relevantSPARQLBlocks, varIndex, currentLine)==false){
					if (!relevantSPARQLBlocks.containsKey("DEFAULT")){
						String outerBlock = extractOuterBlock(varIndex);
						relevantSPARQLBlocks.put("DEFAULT", outerBlock);
					}
				}					
				break;
			}
			
			if (text.charAt(charIndex)=='{'){
				bracketIndex++;
				if (bracketIndex > maxBracketIndex){
					maxBracketIndex++;
					String currentLine = getCurrentLine(text, charIndex);
					if (addNewGraphBlockMapping(relevantSPARQLBlocks, varIndex, currentLine)){
						break;
					}
				}
			}
			else if (text.charAt(charIndex)=='}'){
				bracketIndex--;
			}
			
			charIndex--;		
		}
	}

	private boolean addNewGraphBlockMapping(HashMap<String, String> relevantSPARQLBlocks, int varIndex, String currentLine) {
		if (currentLine.indexOf("GRAPH")!=-1){
			String graphName = extractFirstPattern(currentLine, "GRAPH\\s*((<[^>]*>)|(\\?\\w*))\\s*\\{", 1);
			if (!relevantSPARQLBlocks.containsKey(graphName)){
				String outerBlock = extractOuterBlock(varIndex);
				relevantSPARQLBlocks.put(graphName, outerBlock);
			}
			return true;
		}
		return false;
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
		String currentLine = getCurrentLine(text, varIndex);
		
		for (int i = 0; i < SPARQL_KEYWORDS.length; i++) {
			if (currentLine.indexOf(SPARQL_KEYWORDS[i])!=-1)
				return true;
		}
		return false;
	}

	private String getCurrentLine(String text, int currentIndex) {
		int lastEOL = text.lastIndexOf('\n', currentIndex);
		int nextEOL = text.indexOf('\n', currentIndex);
		if (nextEOL!=-1){ 
			return text.substring(lastEOL+1, nextEOL);
		}
		else{
			return text.substring(lastEOL+1);
		}
	}

	private String extractOuterBlock(int varIndex){
		int openBracket = text.lastIndexOf("{", varIndex);
		int bracketIndex=1;
		int charIndex=openBracket+1;
		while (bracketIndex!=0 && charIndex<text.length()){
			if (text.charAt(charIndex)=='{'){
				bracketIndex++;
			}
			else if (text.charAt(charIndex)=='}'){
				bracketIndex--;
			}
			
			charIndex++;
		}
		
		openBracket = openBracket==-1 ? 0 : openBracket;
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
