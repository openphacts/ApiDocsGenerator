package extractor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TextExtractor {

	protected String text;

	public TextExtractor(String text) {
		super();
		this.text = text;
	}

	protected String extractFirstPattern(String text, String pattern, int groupNumber) {
		Pattern p1 = Pattern.compile(pattern);
		Matcher matcher = p1.matcher(text);			
		
		if (matcher.find()){
			return matcher.group(groupNumber);
		}
		
		return null;
	}
	
	protected String extractSecondPattern(String text, String pattern, int groupNumber) {
		Pattern p1 = Pattern.compile(pattern);
		Matcher matcher = p1.matcher(text);			
		
		String ret =null;
		int count=0;
		while (matcher.find()){
			ret = matcher.group(groupNumber);
			if (count==1){
				return ret;
			}
			else{
				count++;
			}
		}
		
		return ret;
	}
	
}
