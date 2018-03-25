package de.roo.configuration;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * 
 * 
 * @author Leo Nobach
 *
 */
public class Configuration {

	
	static final String DELIMITER = "=";
	
	/**
	 * 
	 * Loads a <b>writable</b> configuration from a INI-like
	 * text file. [name]=[value]
	 * 
	 * @param f
	 * @return
	 * @throws IOException
	 */
	public static IWritableConf loadFromFile(File f) throws IOException {
		FileReader is = new FileReader(f);
		BufferedReader buf = new BufferedReader(is);
		
		Map<String, String> confMap = new HashMap<String, String>();
		
		String line;
		while ((line = buf.readLine()) != null) {
			
			int delimiterPos = line.indexOf(DELIMITER);
			
			if (delimiterPos >= 0 ) {
				String key = line.substring(0, delimiterPos);
				String value = delimiterPos+1 < line.length()?line.substring(delimiterPos +1):"";
				confMap.put(key, value);
			}
		}
		
		buf.close();
		is.close();
		
		return new DefaultConfiguration(confMap);
		
	}
	
	/**
	 * 
	 * @param f
	 * @param conf
	 * @throws IOException
	 */
	public static void saveToFile(File f, IConf conf) throws IOException {
		Map<String, String> values = conf.getAsMap();
		
		FileWriter wr = new FileWriter(f);
		BufferedWriter buf = new BufferedWriter(wr);
		
		for (Entry<String, String> e : values.entrySet()) {
			buf.write(e.getKey() + DELIMITER + e.getValue() + "\n");
		}
		
		buf.close();
		wr.close();
	}
	
	public static IWritableConf loadFromFileIfThere(File f) {
		if (f.exists()) {
			try {
				return loadFromFile(f);
			} catch (IOException e) {
				e.printStackTrace();
				System.err.println("Problems while loading configuration. Using fresh one.");
			}
		}
		return new DefaultConfiguration();
	}
	
	public static void makePathAndSave(File f, IConf conf) throws IOException {
		File folder = f.getParentFile();
		if (folder == null) saveToFile(f, conf);
		else if (!folder.exists()) {
			folder.mkdirs();
		}
		saveToFile(f, conf);
	}
	
}
