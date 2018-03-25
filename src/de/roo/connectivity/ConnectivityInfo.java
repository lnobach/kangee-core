package de.roo.connectivity;

import java.net.InetAddress;

/**
 * 
 * @author Leo Nobach
 *
 */
public class ConnectivityInfo {

	public static final int DEFAULT_ROO_PORT = 23414;
	
	InetAddress lanIP;
	InetAddress inetIP;
	int port;
	
	public ConnectivityInfo(InetAddress lanIP, InetAddress inetIP, int port) {
		this.lanIP = lanIP;
		this.inetIP = inetIP;
		this.port = port;
	}
	
	public InetAddress getInetIP() {
		return inetIP;
	}

	public void setInetIP(InetAddress inetIP) {
		this.inetIP = inetIP;
	}

	public InetAddress getLanIP() {
		return lanIP;
	}

	public void setLanIP(InetAddress myIP) {
		this.lanIP = myIP;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}
	
}
