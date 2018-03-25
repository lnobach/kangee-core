package de.roo.util;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * 
 * @author Leo Nobach
 *
 */
public class StringToolkit {

	public static String getSystemEncoding() {
		return java.nio.charset.Charset.defaultCharset().name();
	}
	
	public static String encodeURL(String str) {
		try {
			return URLEncoder.encode(str, StringToolkit.getSystemEncoding());
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}

	public static List<String> split(String collStr, String regex) {
		return Arrays.asList(collStr.split(regex));
	}

	public static String merge(Collection<String> tokens, String splitChars) {
		StringBuilder b = new StringBuilder();
		boolean start = true;
		for (String token : tokens) {
			if (!start) b.append(splitChars);
			start = false;
			b.append(token);
		}
		return b.toString();
	}
	
}
