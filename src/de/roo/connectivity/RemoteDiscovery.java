package de.roo.connectivity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import de.roo.BuildConstants;

/**
 * 
 * @author Leo Nobach
 *
 */
public class RemoteDiscovery {

	static final String DELIMITER = "=";
	
	/**
	 * if expectedRooID is null, it will NOT be checked!
	 * @param discoveryURL
	 * @param port
	 * @param checkConnectivity
	 * @param expectedRooID
	 * @return
	 * @throws DiscoveryException
	 */
	public static DiscoveryInfo discover(String discoveryURL, int port, boolean checkConnectivity, String expectedRooID) throws DiscoveryException {
		URL url;
		try {
			url = new URL(discoveryURL + "?port=" + port + "&ping=" + (checkConnectivity?"1":"0"));
			HttpURLConnection c = (HttpURLConnection)url.openConnection();
			
			c.setConnectTimeout(BuildConstants.DISTR_SERVER_CONNECT_TIMEOUT);
			c.setReadTimeout(BuildConstants.DISTR_SERVER_READ_TIMEOUT);
			
			BufferedReader r = new BufferedReader(new InputStreamReader(c.getInputStream()));
			
			Map<String, String> tokens = new HashMap<String, String>();
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
			
			r.close();
			c.disconnect();
			
			String success = tokens.get("SUCCESS");
			if (success == null) return null;
			String ipStr = tokens.get("IPADDR");
			if (ipStr == null) return null;
			InetAddress addr = InetAddress.getByName(ipStr);
			
			CheckConnectivityState state = CheckConnectivityState.UNCHECKED;
			String errStr = null;
			int errNo = 0;
			if (checkConnectivity) {
				boolean reachable = Boolean.parseBoolean(tokens.get("REACHABLE"));
				String rooID = tokens.get("ROO_ID");
				boolean correctID = expectedRooID == null || expectedRooID.equals(rooID);
				if (reachable && correctID) state = CheckConnectivityState.OK;
				else {
					state = CheckConnectivityState.ERROR;
					if (reachable && !correctID) {
						errNo = 1001;
						errStr = "Found other Roo device instead: " + rooID + ". You have the ID " + expectedRooID;
					}
					else {
						errStr = tokens.get("PING_ERRSTR");
						try {
							errNo = Integer.parseInt(tokens.get("PING_ERRNO"));
						} catch (NumberFormatException e) {
							errNo = -1;
						}
					}
				}
			}
			return new DiscoveryInfo(addr, state, errStr, errNo);
			
		} catch (MalformedURLException e) {
			throw new DiscoveryException("The discovery URL is malformed.", e);
		} catch (IOException e) {
			throw new DiscoveryException("The discovery server could not be contacted ", e);
		}
	}
	
	public static class DiscoveryInfo {
		
		public String getCheckErrorStr() {
			return errorStr;
		}

		public CheckConnectivityState getCheckState() {
			return state;
		}

		public InetAddress getDiscoveredAddr() {
			return discoveredAddr;
		}

		public int getCheckErrorNum() {
			return errorNum;
		}

		private String errorStr;
		private CheckConnectivityState state;
		private InetAddress discoveredAddr;
		private int errorNum;

		public DiscoveryInfo(InetAddress discoveredAddr, CheckConnectivityState state, String errorStr, int errorNum) {
			this.discoveredAddr = discoveredAddr;
			this.state = state;
			this.errorStr = errorStr;
			this.errorNum = errorNum;
		}
		
		public String toString() {
			return "DiscoveryInfo(" + discoveredAddr + ", " + state + ", " + errorStr + ", " + errorNum + ")";
		}
	}
	
	public static enum CheckConnectivityState {
		OK,
		ERROR,
		UNCHECKED;
	}
	
}
