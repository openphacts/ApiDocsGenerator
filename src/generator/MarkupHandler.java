package generator;

import java.util.Iterator;

public class MarkupHandler implements Iterator<String> {

	private String[] templateLines;
	private int currentLineIndex = 0;
	private String currentLine = "";
	private StringBuffer markupedTemplate;
	private ColorCoding colorCoding;
	
	private final String OPEN_SPAN_TEMPLATE = "<span style=\\\"BACKGROUND-COLOR: #{colorCode}\\\">";
	private final String CLOSING_SPAN = "</span>";
	private char currentLineEnding;
	

	public MarkupHandler(String template, ColorCoding colorCoding) {
		super();
		this.templateLines = template.split("\r?\n");
		markupedTemplate = new StringBuffer();
		this.colorCoding = colorCoding;
	}

	@Override
	public boolean hasNext() {
		if (currentLineIndex<templateLines.length){
			return true;
		}
		else{
			return false;
		}
	}

	@Override
	public String next() {
		currentLine = templateLines[currentLineIndex++].trim();
		currentLineEnding = currentLine.charAt(currentLine.length()-1);
		currentLine = currentLine.substring(0, currentLine.length()-1).trim();
		return currentLine;
	}

	@Override
	public void remove() {
		throw new RuntimeException("Unimplemented");
	}
	
	public void applyMarkup(int option){
		String openSpan = OPEN_SPAN_TEMPLATE.replaceFirst("\\{colorCode\\}", colorCoding.getCode(option));
		
		String[] tokens = currentLine.split("\\s");
		
		String markupedLine = "";
		String predicate, object;
		if (tokens.length == 3){
			markupedLine+=tokens[0]+" ";
			predicate = tokens[1];
			object = tokens[2];
		}
		else if (tokens.length == 2){
			markupedLine+="\t ";
			predicate = tokens[0];
			object = tokens[1];
		}
		else throw new RuntimeException("Unexpected number of tokens on a line");
		
		markupedLine+=openSpan+" "+predicate+" "+object+" "+
				CLOSING_SPAN+" "+currentLineEnding;
		
		System.out.println("Markuped line: "+markupedLine);
		markupedTemplate.append(markupedLine+"\n");
	}
	
	public String getMarkupedTemplate(){
		return markupedTemplate.toString();
	}

	

}
