package extractor;



public class ConfigExtractor extends TextExtractor {

	private static final String API_WHERE_REGEX = "(.*?)api:where\\s*\\\"([^\\\"]*)\\\"";
	private static final int ITEM_ENDPOINT = 0;
	private static final int LIST_ENDPOINT = 1;
	private static final int EXTERNAL_SERVICE_ENDPOINT = 2;
	
	private static final String API_TEMPLATE_REGEX = "(.*?)api:template\\s*\\\"([^\\\"]*)\\\"";
	
	private int endpointType;
	public ConfigExtractor(String text) {
		super(text);
		
		if (text.indexOf("api:ItemEndpoint")!=-1){
			endpointType = ITEM_ENDPOINT;
		}
		else if (text.indexOf("api:ListEndpoint")!=-1){
			endpointType = LIST_ENDPOINT;
		}
		else if (text.indexOf("api:ExternalHTTPService")!=-1){
			endpointType = EXTERNAL_SERVICE_ENDPOINT;
		}
		
	}
	
	public String getApiTemplate(){
		return extractFirstPattern(text, API_TEMPLATE_REGEX, 2);
		
	}
	
	public String getWhereClause(){
		if (endpointType==ITEM_ENDPOINT||endpointType==EXTERNAL_SERVICE_ENDPOINT){
			return extractFirstPattern(text, 
				API_WHERE_REGEX, 2);
		}
		else{
			return extractSecondPattern(text, 
					API_WHERE_REGEX, 2);
		}
	}
}
