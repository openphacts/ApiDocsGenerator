package generator;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TextExtractor {

	protected String text;

	public TextExtractor(String text) {
		super();
		this.text = text;
	}

	protected String extractPatternOnce(String text, String pattern, int groupNumber) {
		Pattern p1 = Pattern.compile(pattern);
		Matcher matcher = p1.matcher(text);			
		
		if (matcher.find()){
			return matcher.group(groupNumber);
		}
		
		return null;
	}
	
}
