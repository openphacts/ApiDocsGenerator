package generator;

import java.util.Iterator;

public class MarkupHandler implements Iterator<String> {

	private String[] templateLines;
	private int currentLine = 0;
	private StringBuffer markupedTemplate;

	public MarkupHandler(String template) {
		super();
		this.templateLines = template.split("\r?\n");
		markupedTemplate = new StringBuffer();
	}

	@Override
	public boolean hasNext() {
		if (currentLine<templateLines.length){
			return true;
		}
		else{
			return false;
		}
	}

	@Override
	public String next() {
		return templateLines[currentLine++];
	}

	@Override
	public void remove() {
		throw new RuntimeException("Unimplemented");
	}
	
	public void applyMarkup(int option){
		//TODO
	}

	

}
