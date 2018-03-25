package de.roo.util.stream;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * 
 * @author Leo Nobach
 *
 */
public class StreamToolkit {

	public static String readLine(InputStream is) throws IOException {
		
		boolean cBefore = false;
		
		ByteArrayOutputStream buf = new ByteArrayOutputStream();
		
		int cur = is.read();
		while(cur != '\n' && !cBefore) {
			if (cur < 0) return null;
			if (cBefore) buf.write('\r');
			cBefore = cur == '\r';
			if (!cBefore) buf.write(cur);
			cur = is.read();
		}
		
		return new String(buf.toByteArray());
		
	}
	
}
