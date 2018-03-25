package de.roo.portmapping;

import java.net.InetAddress;
import java.util.List;

import de.roo.logging.ILog;

/**
 * 
 * @author Leo Nobach
 *
 */
public interface IPortMappingDevice {

	enum Protocol {
		TCP,
		UDP;
	}
	
	public IPortMappingEntry forwardPort(int lanPort, int wanPort, Protocol prot, InetAddress internalClient, String description, ILog log) throws PortMappingException;
	
	public InetAddress getExternalIPAddress(ILog log) throws PortMappingException;
	
	public List<IPortMappingEntry> getPortMappingEntryList(int listLimit, ILog log) throws PortMappingException;
	
	public void deletePortMapping(int wanPort, Protocol prot, ILog log) throws PortMappingException;
	
	/**
	 * Returns null if no entry was found
	 * @param extPort
	 * @param prot
	 * @param log
	 * @return
	 * @throws PortMappingException
	 */
	public IPortMappingEntry getPortMapping(int extPort, Protocol prot, ILog log) throws PortMappingException;
	
	public String getID();
	
	public String getName();
	
	public String getTypeName();
	
	public String getPresentationURL();
	
}
