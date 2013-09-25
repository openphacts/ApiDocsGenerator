package generator;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class Main {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		BufferedReader reader=null;
		try {
					
			reader = new BufferedReader(new FileReader("test"));
			StringBuffer text = new StringBuffer();
			String line;
			while ((line=reader.readLine())!=null){
				text.append(line);
			}
					
			ArrayList<String> apiTemplate = extractPattern(text.toString(), "(.*)api:template\\s*\\\"([^\\\"]*)\\\"(.*)", 2);
			ArrayList<String> wherePattern = extractPattern(text.toString(), "(.*)api:where\\s*\\\"([^\\\"]*)\\\"(.*)", 2);
			if (apiTemplate.size()!=1 || wherePattern.size()!=1){
				System.err.println("More than one api:template or api:where pattern");
				reader.close();
				return;
			}	
			
			HashMap<Integer, String> optionals = extractOptionals(wherePattern.get(0), "?mw_freebase");
			if (!optionals.isEmpty()&&!optionals.containsKey(-1)){
				System.out.println("Optionals names: ");
				for (String optional : optionals.values()) {
					System.out.println(optional);
				}
				System.out.println();
			}
			else{
				//extract the immediately outer block
				HashMap<String, String> blocks = extractGraphNamesAndOuterBlocks(wherePattern.get(0), "?mw_freebase");
				System.out.println("SPARQL Blocks: ");
				for (Map.Entry<String, String> entry : blocks.entrySet()) {
					System.out.println("Graph: "+entry.getKey()+" ; OuterBlock: "+entry.getValue());
				}
				System.out.println();				
			}		
			
			reader.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
		
	private static HashMap<Integer, String> extractOptionals(String text, String var) {
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
				int lastClosingBracket = findClosingBracket(text, text.indexOf('{', lastIndexOfOptional));
				if (varIndex<lastClosingBracket){
					optionalClauses.put(lastIndexOfOptional, text.substring(lastIndexOfOptional, lastClosingBracket));
				}
				else{
					optionalClauses.put(-1, var);//shows there are triple patterns which are not OPTIONAL
				}
			}
			else{
				optionalClauses.put(-1, var);
			}
							
			fromIndex = varIndex+var.length()+1;
		}
		
		return optionalClauses;
	}
	
	private static String extractDefaultGraph(String text, String var) {
		String keyword = "(UNION|OPTIONAL|GRAPH)";
		String defaultGraph = "(.*?)"+keyword;
		String pattern = extractPatternOnce(text, defaultGraph, 1);
		
		return pattern;
	}
	
	private static HashMap<String, String> extractGraphNamesAndOuterBlocks(String text, String var) {
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
			
			lastIndexOfGraph = text.lastIndexOf("GRAPH", varIndex);
			if (lastIndexOfGraph!=-1){
				int lastClosingBracket = findClosingBracket(text, text.indexOf('{', lastIndexOfGraph)+1);
				if (varIndex>lastClosingBracket){
					throw new RuntimeException("Assumption violated: required triple patterns should appear before OPTIONAL, UNION, GRAPH clauses");
				}
				else{
					int graphNameStart = text.indexOf('<', lastIndexOfGraph)+1;
					int graphNameEnd = text.indexOf('>', lastIndexOfGraph);
					String graphName = text.substring(graphNameStart, graphNameEnd);			
					
					//ASSUMPTION: if a variable appears multiple times in a block, show only the first extracted outer block
					if (!relevantSPARQLBlocks.containsKey(graphName)){
						String outerBlock = extractOuterBlock(text, varIndex);
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
	
	private static String extractOuterBlock(String text, int varIndex){
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
	
	private static int findClosingBracket(String text, int fromIndex){
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

	private static ArrayList<String> extractPattern(String text, String pattern, int groupNumber) {
		Pattern p1 = Pattern.compile(pattern);
		Matcher matcher = p1.matcher(text);			
		
		ArrayList<String> ret = new ArrayList<String>();
		while (matcher.find()){
			ret.add(matcher.group(groupNumber));
		}
		
		return ret;
	}
	
	private static String extractPatternOnce(String text, String pattern, int groupNumber) {
		Pattern p1 = Pattern.compile(pattern);
		Matcher matcher = p1.matcher(text);			
		
		if (matcher.find()){
			return matcher.group(groupNumber);
		}
		
		return null;
	}

	private static ArrayList<String> extractSurroundingGraphNameUsingRegex(String text, String var) {
		String inGraphKeyword = "(UNION|OPTIONAL)";
		String SHORT_NOTATION = "([^;\r\n]*;)*[^\\.\r\n]*\\.";
		String FULL_TRIPLE = "([^\\.\n\r]+\\.";
		String subjectBlock = "("+SHORT_NOTATION+" | "+FULL_TRIPLE+")";
		String sparqlBlock = "(\\s*"+inGraphKeyword+"?\\s*\\{[^\\}]*\\})|"+subjectBlock;
		
		//String SHORT_NOTATION_INTEREST = "([^;\r\n]*;)*[^\\.\r\n]*\\.";
		//String FULL_TRIPLE = "([^\\.\n\r]+\\.";
		//String subjectBlock = "("+SHORT_NOTATION+" | "+FULL_TRIPLE+")";
		String interestBlock = "(\\s*"+inGraphKeyword+"?\\s*\\{[^\\}]*"+var+"[^\\}]*\\})";
		ArrayList<String> graphNames = extractPattern(text, "GRAPH\\s*([^\\{]*)\\s*((\\{"+
							sparqlBlock+"*"+interestBlock+sparqlBlock+
							"*\\})|"+
							interestBlock+")", 1);
		return graphNames;
	}

	private static void testGreedyBehavior(){
		String pattern = "(.*?)bla";
		
		String text = "fafsdabla a fdfdbla";
		ArrayList<String> matches = extractPattern(text, pattern, 1);
		
		if (!matches.isEmpty()){
			System.out.println("Optionals names: ");
			for (String match : matches) {
				System.out.println(match);
			}
			System.out.println();
		}
		
	}

	private static ArrayList<String> extractOptionalUsingRegex(String wherePattern, String var) {
		String keyword = "(UNION|GRAPH\\s*<[^<]*>)";
		String sparqlBlock = "(\\s*"+keyword+"?\\s*\\{[^\\}]*\\})";
		String interestBlock = "(\\s*"+keyword+"?\\s*\\{" +
				"[^\\}]*"+var+"[^\\}]*" +
				"\\})";
		String optionalBlock = "(.*)(\\s*OPTIONAL" +
				sparqlBlock+"*"+interestBlock+sparqlBlock+"*"+
				")(.*?)";
		
		ArrayList<String> optional = extractPattern(wherePattern, optionalBlock, 2);
		return optional;
	}
}
