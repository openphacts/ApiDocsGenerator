package generator;

import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import extractor.SPARQLBlockExtractor;

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
		
		Pattern p1 = Pattern.compile(SPARQLBlockExtractor.TRIPLE_PATTERN);
		Matcher matcher = p1.matcher(currentLine);		
		
		String markupedLine = "";
		String predicate, object;
		if (matcher.find()){
			if (matcher.group(1)!=null){
				markupedLine+=matcher.group(1)+" ";
			}
			else{
				markupedLine+="\t";
			}
			predicate = matcher.group(5);
			object = matcher.group(8);
		}		
		else throw new RuntimeException("Unexpected triple pattern");
		
		markupedLine+=openSpan+" "+predicate+" "+object+" "+
				CLOSING_SPAN+" "+currentLineEnding;
		
		System.out.println("Markuped line: "+markupedLine);
		markupedTemplate.append(markupedLine+"\n");
	}
	
	public String getMarkupedTemplate(){
		return markupedTemplate.toString();
	}

	

}
