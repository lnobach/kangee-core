package de.roo.portmapping;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import de.roo.httpcli.HttpClient;
import de.roo.httpcli.HttpClientException;
import de.roo.httpcli.HttpRequest;
import de.roo.httpcli.HttpResponse;
import de.roo.httpcli.IResponseHandler;
import de.roo.logging.ILog;

public class Holepunch {

	static final String DELIMITER = "=";
	
	public static HolepunchInfo getPortFromHolepunch(ILog log) throws IOException, HttpClientException {
		
		
		URL url = new URL("http://getkangee.com/api/holepunch.php");
		HttpClient cli = new HttpClient(new InetSocketAddress(InetAddress.getByName(url.getHost()), 80));
		HttpRequest req = new HttpRequest(url);
		
		cli.setKeepAlive(false);
		
		ResponseHandlerImpl respH = new ResponseHandlerImpl();
		
		cli.makeRequest(req, log, respH);
		
		String port = respH.getResponseTokens().get("PORT");
		
		if (port == null) throw new IllegalArgumentException("The holepunch script did not return a port.");
		
		int externalPort;
		
		try {
			externalPort =  Integer.parseInt(port);
		} catch (NumberFormatException e) {
			throw new IllegalArgumentException("The port returned by the holepunch script is non-numeric: " + port, e);
		}
		
		return new HolepunchInfo(respH.getInternalPortUsed(), externalPort);
		
	}
	
	public static class HolepunchInfo {
		
		final int internalPort;
		
		final int externalPort;

		public HolepunchInfo(int internalPort, int externalPort) {
			super();
			this.internalPort = internalPort;
			this.externalPort = externalPort;
		}

		public int getInternalPort() {
			return internalPort;
		}

		public int getExternalPort() {
			return externalPort;
		}

		@Override
		public String toString() {
			return "HolepunchInfo [internalPort=" + internalPort
					+ ", externalPort=" + externalPort + "]";
		}
		
	}
	
	static class ResponseHandlerImpl implements IResponseHandler {

		final Map<String, String> tokens = new HashMap<String, String>();
		
		int internalPort = -1;
		
		@Override
		public void handleResponse(HttpResponse resp) throws IOException {
			
			
			BufferedReader r = new BufferedReader(new InputStreamReader(resp.getInputStream()));
			
			String line;
			while ((line = r.readLine()) != null) {
				int delimiterPos = line.indexOf(DELIMITER);
				
				if (delimiterPos >= 0 ) {
					String key = line.substring(0, delimiterPos);
					String value = delimiterPos+1 < line.length()?line.substring(delimiterPos +1):"";
					tokens.put(key, value);
					//System.out.println("Added token: " + key + " => " + value);
				}
			}
			
			internalPort = resp.getClientPort();
			
			r.close();
		}
		
		public Map<String, String> getResponseTokens() {
			return tokens;
		}
		
		public int getInternalPortUsed() {
			return internalPort;
		}
		
	}
	
}



