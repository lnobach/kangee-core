package de.roo.portmapping;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import de.roo.logging.ILog;
//import de.roo.portmapping.dummy.DummyDiscovery;
import de.roo.portmapping.IPortMappingDevice.Protocol;
import de.roo.portmapping.PortMappingException.PortMappingConflict;
import de.roo.portmapping.upnp.UPnPPortMappingDiscovery;

/**
 * 
 * @author Leo Nobach
 *
 */
public class PortMapping {
	
	public static List<IPortMappingDevice> discoverAllDevices(ILog log) throws PortMappingException {
		
		List<IDiscoveryMechanism> mechs = new ArrayList<IDiscoveryMechanism>(2);
		
		mechs.add(new UPnPPortMappingDiscovery());
		//mechs.add(new DummyDiscovery()); //TODO: DEBUG
		
		List<IPortMappingDevice> l = new LinkedList<IPortMappingDevice>();
		
		for (IDiscoveryMechanism mech : mechs) {
			log.dbg(PortMapping.class, "Discovering devices for portmapping mechanism type " + mech.getTypeName() + ".");
			l.addAll(mech.discoverPortMappingDevices(log));
			log.dbg(PortMapping.class, "Device discovery for portmapping mechanism type " + mech.getTypeName() + " finished.");
		}
		return l;
	}
	
	
	/**
	 * Does port forwarding (same port for src and dest) and automatically increments the port if it is already used.
	 * 
	 * Returns a potential new port auto-incremented
	 * @param port
	 * @return
	 * @throws PortMappingException
	 */
	public static IPortMappingEntry forwardAutoIncrement(IPortMappingDevice dev, ILog log, int port, Protocol prot, String description, InetAddress localAddr, int limit) throws PortMappingException {
		IPortMappingEntry hdl;
		int inc = 0;
		while(true) {
			try {
				int p2 = port + inc;
				hdl = dev.forwardPort(p2, p2, prot, localAddr, description, log);
				return hdl;
			} catch (PortMappingConflict e) {
				if (inc >= limit) throw e;
				port++;
				log.warn(PortMapping.class, "Port mapping conflict. Auto-incrementing: Will use " + port + " in the next try.", e);
			}
		}
	}
	
}
