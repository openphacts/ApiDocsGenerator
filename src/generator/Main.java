package generator;

import java.io.IOException;


public class Main {

	public static void main(String[] args) {
		MainFlowHandler mainFlow = new MainFlowHandler(true);
		try {
			mainFlow.runFlow();
		} catch (IOException e) {
			
			e.printStackTrace();
		}
	}
	
	/**
	 
	 * public static void main(String[] args) {
		String currentLine = "bla bla2 adf.";
		String[] tokens = currentLine.split("[\\s;.]");
		System.out.println("Length: "+tokens.length);
		for (int i = 0; i < tokens.length; i++) {
			System.out.println(tokens[i]);
		}
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
	}*/
}
