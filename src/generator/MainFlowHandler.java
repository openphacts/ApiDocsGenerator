package generator;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Scanner;

import extractor.ConfigExtractor;
import extractor.SPARQLBlockExtractor;

public class MainFlowHandler {
	
	private static final String DOC_FILE_EXTENSION = ".doc";
	//private static final String API_CONFIG_DIR = "/var/www/html/api-config-files";
	private static final String API_CONFIG_DIR = "/home/sever/git/DocsGenerator/api-config-files";
	private static final String COLOR_CODES_FILE = API_CONFIG_DIR+"/colorCoding/ColorCodes";
	private static final String DOCS_FOLDER_PATH = API_CONFIG_DIR+"/docs";

	public void runFlow() throws IOException{
		ColorCoding colorCoding = new ColorCoding(new File(COLOR_CODES_FILE));
		File configFolder = new File(API_CONFIG_DIR);
		
		for (final File fileEntry : configFolder.listFiles()) {
	        String configFileName = fileEntry.getName();
	        
			if (configFileName.endsWith(".ttl")) {
				System.out.println("Applying markup to the file: "+configFileName+"\n");
				
	            BufferedReader reader = new BufferedReader(new FileReader(fileEntry));
	            String text = readEntireFile(reader);
	            
	            ConfigExtractor configExtractor = new ConfigExtractor(text);
	            String apiTemplate = configExtractor.getApiTemplate().trim();
	            String whereClause = configExtractor.getWhereClause().trim();
	            SPARQLBlockExtractor sparqlExtractor = new SPARQLBlockExtractor(whereClause);
	            
	            MarkupHandler markupHandler = new MarkupHandler(apiTemplate, colorCoding);
	            applyMarkupOnApiTemplate(colorCoding, sparqlExtractor, markupHandler);
	            
	            writeMarkupedTemplateToFile(configFileName, markupHandler);
	            
	            reader.close();
	        } 
	    }	
	}

	private void writeMarkupedTemplateToFile(String configFileName, MarkupHandler markupHandler) throws IOException {
		int dotIndex = configFileName.lastIndexOf('.');
		String docsFileName = configFileName.substring(0, dotIndex)+DOC_FILE_EXTENSION;	   
		FileWriter writer = new FileWriter(DOCS_FOLDER_PATH+"/"+docsFileName);
		writer.write(markupHandler.getMarkupedTemplate());
		writer.close();
	}

	private String readEntireFile(BufferedReader reader) throws IOException {
		char[] cbuf = new char[20000];
		reader.read(cbuf);
		String text = new String(cbuf);
		return text;
	}

	private void applyMarkupOnApiTemplate(ColorCoding colorCoding, SPARQLBlockExtractor sparqlExtractor, MarkupHandler markupHandler) throws IOException {
		Scanner s = new Scanner(System.in);
		while (markupHandler.hasNext()){
			String currentLine = markupHandler.next();
			PrettyPrinter.printCurrentLine(currentLine);
			
			String objectValue = extractObjectFromTriple(currentLine);
			
			extractRelevantBlocksAndPrint(sparqlExtractor, objectValue);
			
			int userOption = promptUserForMarkupOption(colorCoding, s);
			markupHandler.applyMarkup(userOption);
			System.out.println("======================================================================");
		}
		s.close();
	}

	private int promptUserForMarkupOption(ColorCoding colorCoding, Scanner s) {
		System.out.println("Introduce markup OPTION: ");
		System.out.println("Available OPTIONS: "+colorCoding.getAvailableOptions());
		int userOption = s.nextInt();
		if (!colorCoding.checkOptionValidity(userOption)){
			System.err.println("Unknown option");
			System.exit(-1);
		}
		return userOption;
	}

	private void extractRelevantBlocksAndPrint(SPARQLBlockExtractor sparqlExtractor, String objectValue) {
		HashMap<Integer, String> optionals = sparqlExtractor.extractOptionals(objectValue);
		HashMap<String, String> graphBlockMap = sparqlExtractor.extractGraphNamesAndOuterBlocks(objectValue);;
		
		if (!graphBlockMap.isEmpty()){
			PrettyPrinter.printGraphNames(graphBlockMap, objectValue);	
		
			if (optionals.isEmpty() || optionals.containsKey(-1)){//REQUIRED blocks take precedence over OPTIONALS
				PrettyPrinter.printOuterBlocks(graphBlockMap, objectValue);
			}
			else {
				PrettyPrinter.printOPTIONALClauses(optionals, objectValue);
			}
		}
	}

	private String extractObjectFromTriple(String currentLine) {
		String[] tokens = currentLine.split("\\s");
		if (tokens.length>3){
			throw new RuntimeException("Api:Template line with more than 3 tokens");
		}
		if (tokens.length<2){
			throw new RuntimeException("Api:Template line with less than 1 token");
		}
		
		//variable or graph name
		return tokens[tokens.length-1];
	}
	
}
