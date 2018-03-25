package de.roo.srvApi;

import java.io.File;
import java.util.Map;

/**
 * 
 * @author Leo Nobach
 *
 */
public interface IPOSTedFile {

	public File getFile();
	
	public String getHeader(String key);
	
	public Map<String, String> getHeadersAsMap();
	
}
