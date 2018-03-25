package de.roo.util.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

/**
 * 
 * @author Leo Nobach
 *
 */
public class MIMETypes {

	static Set<String> mimeTypes = new HashSet<String>();
	
	static Map<String, List<String>> extsFromMimeType = new HashMap<String, List<String>>();
	static Map<String, String> mimeTypeFromExt = new HashMap<String, String>();
	
	static {
		try {
			load();
		} catch (IOException e) {
			System.err.println("Error while loading MIME types");
			e.printStackTrace();
		}
	}
	
	public static void load() throws IOException {
		load(MIMETypes.class.getResourceAsStream("mime.types"));
	}
	
	public static void load(InputStream is) throws IOException {
		InputStreamReader r = new InputStreamReader(is);
		BufferedReader buf = new BufferedReader(r);
		
		String line;
		while ((line = buf.readLine()) != null) {
			handleLine(line);
		}
	}

	public static void handleLine(String line) {
		int commentPos = line.indexOf('#');
		if (commentPos >= 0) line = line.substring(0, commentPos);
		if ("".equals(line.trim())) return;
		
		String[] parts = line.split("[\t ]+");
		
		if (parts.length > 0) {
			mimeTypes.add(parts[0]);
		}
		if (parts.length > 1) {
			
			List<String> exts = extsFromMimeType.get(parts[0]);
				if (exts == null) {
					exts = new ArrayList<String>(parts.length -1);
					extsFromMimeType.put(parts[0], exts);
				}
			for (int i = 1; i < parts.length; i++) {
				if (!exts.contains(parts[i])) exts.add(parts[i]);
				mimeTypeFromExt.put(parts[i], parts[0]);
			}
			
		}
		
	}
	
	public static List<String> extsFromMIMEType(String mimeType) {
		return extsFromMimeType.get(mimeType);
	}
	
	public static String mimeTypeFromExt(String extension) {
		return mimeTypeFromExt.get(extension);
	}
	
	public static Set<String> getAllMimeTypes() {
		return Collections.unmodifiableSet(mimeTypes);
	}
	
	public static void writeTypesTo(Writer w) throws IOException {
		for (Entry<String, List<String>> e : extsFromMimeType.entrySet()) {
			w.write(e.getKey() + "\t\t ");
			for (String ext : e.getValue()) w.write(ext + " ");
			w.write("\n");
		}
	}

	public static void main(String[] args) {
		try {
			load();
			
			System.out.println("Extension -> MIME =======================");
			for (Entry<String, String> e : mimeTypeFromExt.entrySet()) {
				System.out.println(e);
			}
			
			System.out.println("MIME -> Extension =======================");
			for (Entry<String, List<String>> e : extsFromMimeType.entrySet()) {
				System.out.println(e);
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}
