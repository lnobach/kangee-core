package de.roo.httpsrv;

import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketAddress;

import de.roo.srvApi.IRequesterInfo;

/**
 * 
 * @author Leo Nobach
 *
 */
public class RequesterInfo implements IRequesterInfo {

	private InetAddress requesterIP;
	private SocketAddress requesterSocket;
	private InetAddress myIP;
	private int myPort;
	private SocketAddress mySocket;

	public RequesterInfo(Socket connSocket) {

		requesterIP = connSocket.getInetAddress();
		requesterSocket = connSocket.getRemoteSocketAddress();

		myIP = connSocket.getLocalAddress();
		myPort = connSocket.getLocalPort();
		mySocket = connSocket.getLocalSocketAddress();

	}

	@Override
	public InetAddress getRequesterIP() {
		return requesterIP;
	}
	@Override
	public SocketAddress getRequesterSocket() {
		return requesterSocket;
	}
	@Override
	public InetAddress getMyIP() {
		return myIP;
	}
	@Override
	public int getMyPort() {
		return myPort;
	}
	@Override
	public SocketAddress getMySocket() {
		return mySocket;
	}
	
	public String toString() {
		return requesterIP.toString() + ":" + requesterSocket;
	}
}
