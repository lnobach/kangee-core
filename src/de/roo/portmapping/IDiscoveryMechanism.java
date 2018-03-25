package de.roo.portmapping;

import java.util.List;

import de.roo.logging.ILog;

/**
 * 
 * @author Leo Nobach
 *
 */
public interface IDiscoveryMechanism {
	              
	public List<IPortMappingDevice> discoverPortMappingDevices(ILog log) throws PortMappingException;
	
	public String getTypeName();
	
}
