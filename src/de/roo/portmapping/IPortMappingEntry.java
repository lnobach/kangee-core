package de.roo.portmapping;

import java.net.InetAddress;

import de.roo.logging.ILog;
import de.roo.portmapping.IPortMappingDevice.Protocol;

/**
 * 
 * @author Leo Nobach
 *
 */
public interface IPortMappingEntry {

	public int getLanPort();
	public int getWanPort();
	public Protocol getProtocol();
	public InetAddress getInternalClient();
	public String getDescription();
	
	/**
	 * Returns false if it could not be removed or removal is unsupported.
	 * @return
	 * @throws PortMappingException 
	 */
	public boolean removePortMapping(ILog log) throws PortMappingException;
	
}
