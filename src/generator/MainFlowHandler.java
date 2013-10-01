package generator;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.Scanner;

import extractor.ConfigExtractor;
import extractor.SPARQLBlockExtractor;

public class MainFlowHandler {
	
	private static final String DOC_FILE_EXTENSION = ".doc";
	private static final String API_CONFIG_DIR = "/var/www/html/api-config-files";
	//private static final String API_CONFIG_DIR = "/home/sever/devel/DocsGenerator/api-config-files";
	private static final String COLOR_CODES_FILE = API_CONFIG_DIR+"/colorCoding/ColorCodes";
	private static final String DOCS_FOLDER_PATH = API_CONFIG_DIR+"/docs";
	
	private File configFolder = new File(API_CONFIG_DIR);
	private File[] interestFiles;
	private Scanner scanner;

	public MainFlowHandler() {
		super();	
		interestFiles = configFolder.listFiles();
	}

	public MainFlowHandler(boolean onlyRemaining) {
		super();
		interestFiles = configFolder.listFiles();
		
		if (onlyRemaining==true){	
			removeExistingDocsFromInterestFiles();
		}
	}

	public void runFlow() throws IOException{
		ColorCoding colorCoding = new ColorCoding(new File(COLOR_CODES_FILE));
		scanner = new Scanner(System.in);
		
		System.out.println("Starting iteration on config files..");
		for (final File fileEntry : interestFiles) {
			String configFileName = fileEntry.getName();

			System.out.println("Applying markup to the file: " + configFileName	+ "\n");

			BufferedReader reader = new BufferedReader(new FileReader(fileEntry));
			String text = readEntireFile(reader);

			ConfigExtractor configExtractor = new ConfigExtractor(text);
			String apiTemplate = configExtractor.getApiTemplate();
			String whereClause = configExtractor.getWhereClause();
			if (apiTemplate == null || whereClause == null) {
				System.out.println("Skipping " + configFileName
						+ " ; no api template or where clause ===========\n");
				reader.close();
				continue;
			}

			apiTemplate = apiTemplate.trim();
			whereClause = whereClause.trim();
			SPARQLBlockExtractor sparqlExtractor = new SPARQLBlockExtractor(
					whereClause);

			MarkupHandler markupHandler = new MarkupHandler(apiTemplate,
					colorCoding);
			applyMarkupOnApiTemplate(colorCoding, sparqlExtractor,
					markupHandler);

			writeMarkupedTemplateToFile(configFileName, markupHandler);

			reader.close();

	    }	
		System.out.println("Iteration on config files finished");
		
		scanner.close();
	}

	private void writeMarkupedTemplateToFile(String configFileName, MarkupHandler markupHandler) throws IOException {
		int dotIndex = configFileName.lastIndexOf('.');
		String docsFileName = configFileName.substring(0, dotIndex)+DOC_FILE_EXTENSION;	   
		FileWriter writer = new FileWriter(DOCS_FOLDER_PATH+"/"+docsFileName);
		writer.write(markupHandler.getMarkupedTemplate());
		writer.close();
	}

	private String readEntireFile(BufferedReader reader) throws IOException {
		char[] cbuf = new char[65536];
		reader.read(cbuf);
		String text = new String(cbuf);
		return text;
	}

	private void applyMarkupOnApiTemplate(ColorCoding colorCoding, SPARQLBlockExtractor sparqlExtractor, MarkupHandler markupHandler) throws IOException {
		
		while (markupHandler.hasNext()){
			String currentLine = markupHandler.next();
			PrettyPrinter.printCurrentLine(currentLine);
			
			String objectValue = sparqlExtractor.extractObjectFromTriple(currentLine);
			
			extractRelevantBlocksAndPrint(sparqlExtractor, objectValue);
			
			int userOption = promptUserForMarkupOption(colorCoding, scanner);
			markupHandler.applyMarkup(userOption);
			System.out.println("======================================================================");
		}		
	}

	private int promptUserForMarkupOption(ColorCoding colorCoding, Scanner s) {
		System.out.println("Introduce markup OPTION: ");
		System.out.println("Available OPTIONS: "+colorCoding.getAvailableOptions());
		int userOption=0;
		while (true) {
			try{		
			String line = s.nextLine();
			userOption = Integer.parseInt(line, 10);
			if (!colorCoding.checkOptionValidity(userOption)) {
				System.err.println("Unrecognized option, try again");
				continue;
			}
			break;
			}
			catch(NumberFormatException e){
				System.err.println("NumberFormatException: "+e.getMessage());
			}
			catch(NoSuchElementException e){
				System.err.println("NoSuchElement: "+e.getMessage());
			}
		}
		return userOption;
	}

	private void extractRelevantBlocksAndPrint(SPARQLBlockExtractor sparqlExtractor, String objectValue) {
		HashMap<Integer, String> optionals = sparqlExtractor.extractOptionals(objectValue);
		HashMap<String, String> graphBlockMap = sparqlExtractor.extractGraphNamesAndOuterBlocksEfficiently(objectValue);;
		
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

	private void removeExistingDocsFromInterestFiles() {
		File docsFolder = new File(DOCS_FOLDER_PATH);
		String[] existingDocFileNames = docsFolder.list();
		LinkedList<File> remainingInterestFiles = new LinkedList<File>();
		
		for (File file : interestFiles) {
			if (file.getName().endsWith(".ttl")) {
				boolean include = true;
				for (String fileName : existingDocFileNames) {
					String rootName = fileName.substring(0, fileName.lastIndexOf('.'));
					String ttlFileName = API_CONFIG_DIR + "/" + rootName+ ".ttl";
					if (file.getAbsolutePath().equals(ttlFileName)) {
						include = false;
						break;
					}
				}

				if (include) {
					remainingInterestFiles.add(file);
				}
			}
		}
		
		interestFiles = new File[remainingInterestFiles.size()];
		interestFiles = remainingInterestFiles.toArray(interestFiles);
	}
}
