package de.roo.portmapping.dummy;

import java.util.ArrayList;
import java.util.List;

import de.roo.logging.ILog;
import de.roo.portmapping.IDiscoveryMechanism;
import de.roo.portmapping.IPortMappingDevice;
import de.roo.portmapping.PortMappingException;

/**
 * 
 * @author Leo Nobach (dev@getkangee.com)
 *
 */
public class DummyDiscovery implements IDiscoveryMechanism {

	@Override
	public List<IPortMappingDevice> discoverPortMappingDevices(ILog log)
			throws PortMappingException {
		
		try {
			log.dbg(this, "Sleeping 100 ms to be more realistic.");
			Thread.sleep(100);
		} catch (InterruptedException e) {
			//Nothing to do
		}
		List<IPortMappingDevice> l = new ArrayList<IPortMappingDevice>();
		log.dbg(this, "Adding three dummy devices.");
		l.add(new DummyPortMappingDevice("Duck"));
		l.add(new DummyPortMappingDevice("Goose"));
		l.add(new DummyPortMappingDevice("Swine"));
		return l;
		
	}
	
	@Override
	public String getTypeName() {
		return "Dummy";
	}

}
