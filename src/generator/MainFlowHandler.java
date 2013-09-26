package generator;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

public class MainFlowHandler {

	
	public void runFlow() throws IOException{
		ColorCoding colorCoding = new ColorCoding(new File("/var/www/html/api-config-files/colorCoding"));
		File folder = new File("/var/www/html/api-config-files");
		
		//iterate over config files
		for (final File fileEntry : folder.listFiles()) {
	        if (fileEntry.getName().endsWith(".ttl")) {
	            BufferedReader reader = new BufferedReader(new FileReader(fileEntry));
	            char[] cbuf = new char[2000];
	            reader.read(cbuf);
	            String text = new String(cbuf);
	            
	            ConfigExtractor configExtractor = new ConfigExtractor(text);
	            String apiTemplate = configExtractor.getApiTemplate().trim();
	            String whereClause = configExtractor.getWhereClause().trim();
	            SPARQLBlockExtractor sparqlExtractor = new SPARQLBlockExtractor(whereClause);
	            
	            MarkupHandler markupHandler = new MarkupHandler(apiTemplate);
	            while (markupHandler.hasNext()){
	            	String currentLine = markupHandler.next().trim();
	            	
	            	String[] tokens = currentLine.split("[\\s;.]");
	            	if (tokens.length>3){
	            		throw new RuntimeException("Api:Template line with more than 3 tokens");
	            	}
	            	if (tokens.length<2){
	            		throw new RuntimeException("Api:Template line with less than 1 token");
	            	}
	            	
	            	int userOption;
	            	String lastToken = tokens[tokens.length-1];//variable or graph name
	            	
	            	HashMap<Integer, String> optionals = sparqlExtractor.extractOptionals(lastToken);
	            	HashMap<String, String> required = null;
	            	if (optionals.containsKey(-1)){//REQUIRED blocks take precedence over OPTIONALS
	            		required = sparqlExtractor.extractGraphNamesAndOuterBlocks(lastToken);
	            		//TODO nicely print the blocks to the user
	            	}
	            	else{
	            		//TODO show the OPTIONAL blocks to the user
	            	}
	            	
	            	//prompt the user for the option
	            	userOption = System.in.read();
	            	if (!colorCoding.checkOptionValidity(userOption)){
	            		System.err.println("Unknown option");
	            		System.exit(-1);
	            	}
	            	
	            	markupHandler.applyMarkup(userOption);
	            }
	            
	            reader.close();
	        } 
	    }
		
		
		
		
	}
	
}
