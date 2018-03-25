package de.roo.portmapping;

import java.net.InetAddress;

import de.roo.logging.ILog;

/**
 * 
 * @author Leo Nobach
 *
 */
public interface IWANInetAddressDiscovery {

	public InetAddress discover(ILog log) throws AddressDiscoveryException;
	
}
