package generator;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
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
				System.err.println("More than one api:template or where pattern");
				reader.close();
				return;
			}
			
			ArrayList<String> graphNames = extractSurroundingGraphName(wherePattern.get(0), "?mw_freebase");
			System.out.println("Graph names: ");
			for (String graphName : graphNames) {
				System.out.print(graphName+" | ");
			}
			System.out.println();
			
			ArrayList<String> optionals = extractOptional(wherePattern.get(0), "?mw_freebase");
			if (!optionals.isEmpty()){
				System.out.println("Optionals names: ");
				for (String optional : optionals) {
					System.out.print(optional+" | ");
				}
				System.out.println();
			}
			else{
				//extract the immediately outer block
				
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

	private static ArrayList<String> extractOptional(String wherePattern, String var) {
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

	private static ArrayList<String> extractSurroundingGraphName(String text, String var) {
		String inGraphKeyword = "(UNION|OPTIONAL)";
		String sparqlBlock = "(\\s*"+inGraphKeyword+"?\\s*\\{[^\\}]*\\})";
		String interestBlock = "(\\s*"+inGraphKeyword+"?\\s*\\{[^\\}]*"+var+"[^\\}]*\\})";
		ArrayList<String> graphNames = extractPattern(text, "(.*)GRAPH\\s*([^\\{]*)\\s*((\\{"+
							sparqlBlock+"*"+interestBlock+sparqlBlock+
							"*\\})|"+
							interestBlock+")"+
							"(.*)", 2);
		return graphNames;
	}

	private static ArrayList<String> extractPattern(String text, String apiTemplatePattern, int groupNumber) {
		Pattern p1 = Pattern.compile(apiTemplatePattern);
		Matcher matcher = p1.matcher(text);			
		
		ArrayList<String> ret = new ArrayList<String>();
		while (matcher.find()){
			ret.add(matcher.group(groupNumber));
		}
		
		return ret;
	}

}
