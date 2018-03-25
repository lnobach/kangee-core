package de.roo.util;

import java.util.HashMap;
import java.util.Map;

/**
 * 
 * @author Leo Nobach
 *
 */
public class HTMLToolkit {

	static final Map<Character, String> specialChars = new HashMap<Character, String>();

	static {
		specialChars.put('<', "&lt;");
		specialChars.put('>', "&gt;");
		specialChars.put('&', "&amp;");
		specialChars.put('"', "&quot;");
		specialChars.put('à', "&agrave;");
		specialChars.put('À', "&Agrave;");
		specialChars.put('â', "&acirc;");
		specialChars.put('Â', "&Acirc;");
		specialChars.put('ä', "&auml;");
		specialChars.put('Ä', "&Auml;");
		specialChars.put('å', "&aring;");
		specialChars.put('Å', "&Aring;");
		specialChars.put('æ', "&aelig;");
		specialChars.put('Æ', "&AElig;");
		specialChars.put('ç', "&ccedil;");
		specialChars.put('Ç', "&Ccedil;");
		specialChars.put('é', "&eacute;");
		specialChars.put('É', "&Eacute;");
		specialChars.put('è', "&egrave;");
		specialChars.put('È', "&Egrave;");
		specialChars.put('ê', "&ecirc;");
		specialChars.put('Ê', "&Ecirc;");
		specialChars.put('ë', "&euml;");
		specialChars.put('Ë', "&Euml;");
		specialChars.put('ï', "&iuml;");
		specialChars.put('Ï', "&Iuml;");
		specialChars.put('ô', "&ocirc;");
		specialChars.put('Ô', "&Ocirc;");
		specialChars.put('ö', "&ouml;");
		specialChars.put('Ö', "&Ouml;");
		specialChars.put('ø', "&oslash;");
		specialChars.put('Ø', "&Oslash;");
		specialChars.put('ß', "&szlig;");
		specialChars.put('ù', "&ugrave;");
		specialChars.put('Ù', "&Ugrave;");
		specialChars.put('û', "&ucirc;");
		specialChars.put('Û', "&Ucirc;");
		specialChars.put('ü', "&uuml;");
		specialChars.put('Ü', "&Uuml;");
		specialChars.put('®', "&reg;");
		specialChars.put('©', "&copy;");
		specialChars.put('€', "&euro;");
		specialChars.put(' ', "&nbsp;");
		
		specialChars.put('\n', "<br />");
		specialChars.put('\t', "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;");
	}
	
	public static String encodeSpecialChars(String source) {
		StringBuilder result = new StringBuilder();
		for (int i = 0; i < source.length(); i++) {
			Character c = source.charAt(i);
			String subst = specialChars.get(c);
			if (subst == null) result.append(c);
			else result.append(subst);
		}
		return result.toString();
	}

}
