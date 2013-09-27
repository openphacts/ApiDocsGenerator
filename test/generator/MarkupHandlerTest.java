package generator;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import static org.mockito.Mockito.mock;

public class MarkupHandlerTest {

	private MarkupHandler markupHandler;
	
	
	@Test
	public void test2Rows() {
		markupHandler = new MarkupHandler("row11 row12 row13\nrow21 row22 row23\n", mock(ColorCoding.class));
		int counter=0;
		while (markupHandler.hasNext()){
			markupHandler.next();
			counter++;
		}
		
		assertTrue(counter==2);
		assertTrue(markupHandler.hasNext()==false);
	}
	
	@Test
	public void test3Rows() {
		markupHandler = new MarkupHandler("\nrow11 row12 row13\r\nrow21 row22 row23\n", mock(ColorCoding.class));
		int counter=0;
		while (markupHandler.hasNext()){
			markupHandler.next();
			counter++;
		}
		
		assertTrue(counter==3);
		assertTrue(markupHandler.hasNext()==false);
	}
	
	

}
