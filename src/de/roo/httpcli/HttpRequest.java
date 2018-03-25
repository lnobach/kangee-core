package de.roo.httpcli;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import de.roo.http.ReqMethod;

/**
 * 
 * @author Leo Nobach
 *
 */
public class HttpRequest {
	
	Map<String, String> headers = new HashMap<String, String>();
	private ReqMethod method = ReqMethod.GET;
	private URL url;
	private byte[] postContent;

	public HttpRequest(URL url) {
		super();
		this.url = url;
	}

	public void setMethod(ReqMethod method) {
		this.method = method;
	}
	
	public void setURL(URL url) {
		this.url = url;
	}
	
	public void addHeader(String key, String value) {
		headers.put(key, value);
	}
	
	public void setPOSTContent(byte[] content) {
		postContent = content;
	}

	public URL getURL() {
		return url;
	}

	public Map<String, String> getHeaders() {
		return headers;
	}

	public ReqMethod getMethod() {
		return method;
	}
	
	public byte[] getPostContent() {
		return postContent;
	}
	
}
