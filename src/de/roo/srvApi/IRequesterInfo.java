package de.roo.srvApi;

import java.net.InetAddress;
import java.net.SocketAddress;

/**
 * 
 * @author Leo Nobach
 *
 */
public interface IRequesterInfo {

	public abstract InetAddress getRequesterIP();

	public abstract SocketAddress getRequesterSocket();

	public abstract InetAddress getMyIP();

	public abstract int getMyPort();

	public abstract SocketAddress getMySocket();

}