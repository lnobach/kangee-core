package de.roo.srvApi;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import de.roo.BuildConstants;

/**
 * 
 * @author Leo Nobach
 *
 */
public class ResponseHeaders {

	Map<String, String> headers = new HashMap<String, String>();
	boolean invalidated = false;
	
	public ResponseHeaders(){
		this(true);
	}
	
	public ResponseHeaders(boolean setDefaults) {
		if (setDefaults) {
			addHeader("Content-Type", "text/html; charset=utf-8");
			addHeader("Server", BuildConstants.PROD_TINY_NAME_VER);
		}
	}
	
	public void addHeader(String key, String value) {
		if (invalidated) throw new IllegalArgumentException("Can not add headers, headers already sent.");
		headers.put(key, value);
	}
	
	public String getHeader(String key) {
		return headers.get(key);
	}
	
	public Map<String, String> getAsMap() {
		return Collections.unmodifiableMap(headers);
	}
	
	public void invalidate() {
		invalidated = true;
	}

	public void overwrite(ResponseHeaders hdrs) {
		headers.putAll(hdrs.getAsMap());
	}
	
	public void setContentLength(long cLength) {
		headers.put("Content-Length", String.valueOf(cLength));
	}
	
}
