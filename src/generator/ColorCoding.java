package generator;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class ColorCoding {

	public static final short REQUIRED_SINGLETON = 0;
	public static final short REQUIRED_LIST = 1;
	public static final short OPTIONAL_SINGLETON = 2;
	public static final short OPTIONAL_LIST = 3;
	
	public static final String[] OPTION_NAMES={"REQUIRED_SINGLETON", "REQUIRED_LIST", "OPTIONAL_SINGLETON", "OPTIONAL_LIST"};
	
	private String[] colorCodes = new String[4];

	public ColorCoding(File colorCodingFile) {
		super();
		
		try {
			Scanner sc = new Scanner(colorCodingFile);
			for (int i = 0; i < colorCodes.length; i++) {
				String line = sc.nextLine();
				colorCodes[i] = line.substring(0, line.indexOf(' '));
			}
			
			sc.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public String getCode(int option){
		return colorCodes[option];
	}
	
	public boolean checkOptionValidity(int option){
		if (option<REQUIRED_SINGLETON || option>OPTIONAL_LIST){
			return false;
		}
		return true;
	}
	
	public String getAvailableOptions(){
		String ret="";
		for (int i = 0; i < OPTION_NAMES.length; i++) {
			ret+=i+"-"+OPTION_NAMES[i]+" ; ";
		}
		
		return ret;
	}
	
	
}
