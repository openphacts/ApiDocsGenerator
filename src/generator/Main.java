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

	public static void main(String[] args) {
		String currentLine = "bla bla2 adf ;\n";
		String[] tokens = currentLine.split("[\\s;.]");
		System.out.println("Length: "+tokens.length);
		for (int i = 0; i < tokens.length; i++) {
			System.out.println(tokens[i]);
		}
	}
	/**
	 * @param args
	 */
	public static void main2(String[] args) {
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
			
			/*HashMap<Integer, String> optionals = extractOptionals(wherePattern.get(0), "?mw_freebase");
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
			}	*/	
			
			reader.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

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
