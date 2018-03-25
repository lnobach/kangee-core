package de.roo.httpcli;

import java.io.InputStream;
import java.util.Collections;
import java.util.Map;

import de.roo.http.Version;

/**
 * 
 * @author Leo Nobach
 *
 */
public class HttpResponse {

	private Version respVer;
	private int respCode;
	private Map<String, String> respHeaders;
	private InputStream is;
	private int clientPort;

	public HttpResponse(Version respVer, int respCode,
			Map<String, String> respHeaders, InputStream is, int clientPort) {
		this.respVer = respVer;
		this.respCode = respCode;
		this.respHeaders = respHeaders;
		this.is = is;
		this.clientPort = clientPort;
	}

	public int getClientPort() {
		return clientPort;
	}
	
	public Version getResponseVersion() {
		return respVer;
	}

	public int getResponseCode() {
		return respCode;
	}

	public String getRespHeaderValue(String respHeader) {
		return respHeaders.get(respHeader.toLowerCase());
	}
	
	/**
	 * Unmodifiable and ormalized to lowercase!
	 * @return
	 */
	public Map<String, String> getResponseHeaders() {
		return Collections.unmodifiableMap(respHeaders);
	}
	
	public InputStream getInputStream() {
		return is;
	}

}
