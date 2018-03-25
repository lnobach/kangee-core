package de.roo.util;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.LinkedList;
import java.util.List;

/**
 * 
 * @author Leo Nobach
 *
 */
public class ExceptionToolkit {

	public static String getCauseChainMessagesCRLFSeparated(Throwable e) {
		
		StringBuilder b = new StringBuilder();
		
		Throwable currentE = e;
		while (currentE != null) {
			b.append(currentE.getMessage() + "\n");
			currentE = currentE.getCause();
		}
		
		return b.toString();
	}
	
	public static List<Throwable> getCauseChainMessages(Throwable e) {
		List<Throwable> result = new LinkedList<Throwable>();
		Throwable currentE = e;
		while (currentE != null) {
			result.add(currentE);
			currentE = currentE.getCause();
		}
		return result;
	}

	public static String getStackTraceString(Throwable e) {
		StringWriter strW = new StringWriter();
		PrintWriter pw = new PrintWriter(strW);
		e.printStackTrace(pw);
		return strW.toString();
	}
}
