package de.roo.srv;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 
 * @author Leo Nobach (dev@getkangee.com)
 *
 */
public class POSTHeaderHandlerTest {

	public static void main(String[] args) {
		
		parseContentDisposition("blargh; blubbel-data ; name=\"file\"; filename=\"Testdatei.txt\"");
		
	}
	
	static Pattern cDispP = Pattern.compile("\\G\\s*([\\w-_]+)\\s*(=\\s*\"([^\"]*)\"\\s*)?;?");

	protected static void parseContentDisposition(String cDispStr) {

		Matcher m = cDispP.matcher(cDispStr);
		
		while (m.find()) {
			String name = m.group(1);
			String value; 
			try {
				value = m.group(3);
			} catch (IndexOutOfBoundsException e) {
				value = null;
			}
			
			System.out.println("Found:" + name + ", " + value);
		}
		
	}
	
	
}
