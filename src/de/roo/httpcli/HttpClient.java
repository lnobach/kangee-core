package de.roo.httpcli;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import de.roo.http.ReqMethod;
import de.roo.http.Version;
import de.roo.logging.ILog;
import de.roo.util.stream.LimitedEOFStream;
import de.roo.util.stream.StreamToolkit;

/**
 * 
 * @author Leo Nobach
 *
 */
public class HttpClient {

	private Version version;
	private InetSocketAddress addr;
	Map<String, String> prefHeaders = new HashMap<String, String>();
	
	static final String ret = "\r\n";
	
	Object treeLock = new Object();
	
	Socket s = null;
	private boolean shallKeepAlive = true;

	public HttpClient(InetAddress host, int port) {
		this(new InetSocketAddress(host, port));
	}
	
	public HttpClient(String host, int port) {
		this(new InetSocketAddress(host, port));
	}
	
	public HttpClient(InetSocketAddress addr) {
		this(addr, new Version("HTTP/1.1"));
	}
	
	public HttpClient(InetSocketAddress addr, Version version) {
		this.version = version;
		this.addr = addr;
	}
	
	public void setKeepAlive(boolean keepalive) {
		this.shallKeepAlive = keepalive;
	}
	
	public void makeRequest(HttpRequest req, ILog log, IResponseHandler respHdlr) throws HttpClientException {
		synchronized (treeLock) {
			boolean shallKeepAliveLoc = shallKeepAlive;
			URL url = req.getURL();
			Map<String, String> prefHeaders = req.getHeaders();
			ReqMethod method = req.getMethod();
			byte[] postContent = req.getPostContent();
			if (method == ReqMethod.POST && postContent == null) throw new NullPointerException("POST method is used, but the POST content byte array is null.");
			if (method == null) throw new NullPointerException("The request method may not be null");
			try {
				int urlport = url.getPort();
				if (urlport < 0) urlport = 80;
				
				InetAddress host = InetAddress.getByName(url.getHost());
				if (!addr.getAddress().equals(host) || !(addr.getPort() == urlport)) 
					throw new AssertionError("The host name or port in the URL " + url + "(" + host + ":" + urlport + ") is not equal to " + addr);
			} catch (UnknownHostException e) {
				throw new HttpClientException();
			}
			try {
				if (s == null) {
					s = new Socket(addr.getAddress(), addr.getPort());
				}
				int clientPort = s.getLocalPort();
				
				String reqPath = getReqPath(url);
				Map<String, String> headers = getDefaultHeaders(url, method, postContent, shallKeepAliveLoc);
				headers.putAll(prefHeaders);
				
				BufferedOutputStream os = new BufferedOutputStream(s.getOutputStream());
				OutputStreamWriter wr = new OutputStreamWriter(os);
				//Write the request line.
				wr.write(method + " " + reqPath + " " + version + ret);
				writeHeaders(wr, headers);
				wr.write(ret);
				if (method == ReqMethod.POST) {
					wr.flush();
					os.write(postContent);
				}
				wr.flush();
				os.flush();
				BufferedInputStream is = new BufferedInputStream(s.getInputStream());
				String responseLine = StreamToolkit.readLine(is);
				if (responseLine == null) throw new HttpClientException("Socket prematurely closed by peer.");
				String[] tokens = responseLine.split(" ");
				if (tokens.length < 3) throw new HttpClientException("Malformed response header: " + responseLine);
				Version respVer;
				try {
					respVer = new Version(tokens[0]);
				} catch (IllegalArgumentException e) {
					throw new HttpClientException("Invalid response version: " + tokens[0]);
				}
				int respCode;
				try {
					respCode = Integer.parseInt(tokens[1]);
				} catch (NumberFormatException e) {
					throw new HttpClientException("Status code is not an integer value: " + tokens[1]);
				}
				Map<String, String> respHeaders = readRespHeaders(is);
				long respLength = getResponseLength(respHeaders);
				boolean chunked = isChunked(respHeaders);
				boolean hasRespLength = respLength >= 0;
				shallKeepAliveLoc = shallKeepAliveLoc && (hasRespLength || chunked) && serverWantsKeepAlive(respHeaders);				
				InputStream hdlrIs;
				LimitedEOFStream limStr = null;
				ChunkHandlingStream chStr = null;
				
				if (chunked) {
					chStr = new ChunkHandlingStream(is);
					hdlrIs = chStr;
				} else if (shallKeepAliveLoc) {
					limStr = new LimitedEOFStream(is, respLength);
					hdlrIs = limStr;
				} else hdlrIs = is;
				HttpResponse resp = new HttpResponse(respVer, respCode, respHeaders, hdlrIs, clientPort);
				respHdlr.handleResponse(resp);
				if (chunked) {
					chStr.skipToEOF();
				} else if (shallKeepAliveLoc) {
					limStr.skipToEOF();
				} else {
					System.out.println("Closing...");
					os.close();
					is.close();
					s.close();
					s = null;
				}
			} catch (IOException e) {
				s = null;
				throw new HttpClientException(e);
			}
		}
	}
	
	public void closeConnection() {
		synchronized (treeLock) {
			if (s != null) {
				try {
					s.close();
				} catch (IOException e) {
					//Nothing to do
				}
				s = null;
			}
		}
	}

	private boolean isChunked(Map<String, String> respHeaders) {
		String keepAliveStr = respHeaders.get("transfer-encoding");
		if (keepAliveStr == null) return false;
		return("chunked".equalsIgnoreCase(keepAliveStr));
	}

	private Map<String, String> readRespHeaders(InputStream is) throws IOException, HttpClientException {
		Map<String, String> result = new HashMap<String, String>();
		String line = StreamToolkit.readLine(is);
		while (!"".equals(line)) {
			parseHeaderLine(line, result);
			line = StreamToolkit.readLine(is);
		}
		return result;
	}
	
	public void parseHeaderLine(String headerLine, Map<String, String> target) throws HttpClientException {
		int delimiter = headerLine.indexOf(":");
		if (delimiter == -1) throw new HttpClientException("Header malformed: " + headerLine);
		String key = headerLine.substring(0, delimiter).toLowerCase();	//Some servers return uppercase header names.
		String value = headerLine.substring(delimiter +1);
		if (value.startsWith(" ")) value = value.substring(1);
		target.put(key, value);
	}

	private void writeHeaders(Writer wr, Map<String, String> headers) throws IOException {
		for (Entry<String, String> h : headers.entrySet()) {
			wr.write(h.getKey() + ": " + h.getValue() + ret);
		}
	}

	private String getReqPath(URL url) {
		String result = url.getPath();
		if (result == null || "".equals(result.trim())) result = "/";
		return result;
	}

	private Map<String, String> getDefaultHeaders(URL url,
			ReqMethod method, byte[] postContent, boolean shallKeepAliveLoc) {
		Map<String, String> hs = new HashMap<String, String>();
		int port = url.getPort();
		hs.put("Host", url.getHost() + (port >= 0? ":" + port : ""));
		if (method == ReqMethod.POST) {
			hs.put("Content-Length", String.valueOf(postContent.length));
			hs.put("Content-Type", "text/plain; charset=\"utf-8\"");
		}
		hs.put("Connection", shallKeepAliveLoc?"keep-alive":"close");
		
		return hs;
	}
	

	/**
	 * Returns -1 or less if no response length is given.
	 * @param respHeaders
	 * @return
	 */
	private long getResponseLength(Map<String, String> respHeaders) {
		String respLenStr = respHeaders.get("content-length");
		if (respLenStr == null) return -1;
		try {
			return Long.parseLong(respLenStr);
		} catch (NumberFormatException e) {
			return -1;
		}
	}

	private boolean serverWantsKeepAlive(Map<String, String> respHeaders) {
		String keepAliveStr = respHeaders.get("connection");
		if (keepAliveStr == null) return false;
		return("keep-alive".equalsIgnoreCase(keepAliveStr));
	}
	
}
