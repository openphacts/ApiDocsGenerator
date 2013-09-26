package generator;


public class ConfigExtractor extends TextExtractor {

	public ConfigExtractor(String text) {
		super(text);
	}
	
	public String getApiTemplate(){
		return extractPatternOnce(text.toString(), 
				"(.*)api:template\\s*\\\"([^\\\"]*)\\\"(.*)", 
				2);
	}
	
	public String getWhereClause(){
		return extractPatternOnce(text.toString(), 
				"(.*)api:where\\s*\\\"([^\\\"]*)\\\"(.*)", 
				2);
	}
}
