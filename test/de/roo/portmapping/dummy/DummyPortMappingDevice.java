package de.roo.portmapping.dummy;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import de.roo.logging.ILog;
import de.roo.portmapping.IPortMappingDevice;
import de.roo.portmapping.IPortMappingEntry;
import de.roo.portmapping.PortMappingException;

/**
 * 
 * @author Leo Nobach (dev@getkangee.com)
 *
 */
public class DummyPortMappingDevice implements IPortMappingDevice {

	private String postfix;

	public DummyPortMappingDevice(String postfix) {
		this.postfix = postfix;
	}
	
	@Override
	public IPortMappingEntry forwardPort(final int lanPort, final int wanPort, final Protocol prot,
			final InetAddress internalClient, final String description, ILog log)
			throws PortMappingException {
		log.dbg(this, "Forwarding port on dummy device " + postfix + "Arguments: (" + lanPort + ", " + 
				wanPort + ", " + prot +  ", " + internalClient + ", " + description + ", " + log + ") This has no effect in reality.");
		try {
			Thread.sleep(300);
		} catch (InterruptedException e) {
			//Nothing to do.
		}
		log.dbg(this, "Forwarding on device " + postfix + " finished.");
		return new PortMappingEntryImpl(description, internalClient, lanPort, prot, wanPort);
		
	}

	@Override
	public String getID() {
		return "dummy:" + postfix;
	}

	@Override
	public String getName() {
		return "Dummy device " + postfix;
	}

	@Override
	public String getTypeName() {
		return "Dummy";
	}

	@Override
	public InetAddress getExternalIPAddress(ILog log)
			throws PortMappingException {
		try {
			return InetAddress.getByName("123.123.123.123");
		} catch (UnknownHostException e) {
			throw new PortMappingException(e);
		}
	}

	@Override
	public void deletePortMapping(int wanPort, Protocol prot, ILog log)
			throws PortMappingException {
		log.dbg(this, "Removing port mapping. This is a dummy device, this has no effect.");
	}

	@Override
	public List<IPortMappingEntry> getPortMappingEntryList(int listLimit,
			ILog log) throws PortMappingException {
		
		try {
			InetAddress addr1 = InetAddress.getByName("192.168.0.1");
			InetAddress addr2 = InetAddress.getByName("192.168.0.2");
			InetAddress addr3 = InetAddress.getByName("192.168.0.2");
			List<IPortMappingEntry> list = new ArrayList<IPortMappingEntry>(3);
			list.add(new PortMappingEntryImpl("Descr1", addr1, 11, Protocol.TCP, 11));
			list.add(new PortMappingEntryImpl("Descr2", addr2, 12, Protocol.UDP, 12));
			list.add(new PortMappingEntryImpl("Descr3", addr3, 13, Protocol.TCP, 13));
			return list;
		} catch (UnknownHostException e) {
			//Can not happen
			return null;
		}
		
		
	}

	public class PortMappingEntryImpl implements IPortMappingEntry {
		
		private String description;
		private InetAddress internalClient;
		private int lanPort;
		private Protocol prot;
		private int wanPort;

		public PortMappingEntryImpl(String description, InetAddress internalClient, int lanPort, Protocol prot, int wanPort) {
			this.description = description;
			this.internalClient = internalClient;
			this.lanPort = lanPort;
			this.prot = prot;
			this.wanPort = wanPort;
		}
		
		@Override
		public boolean removePortMapping(ILog log) throws PortMappingException {
			log.dbg(this, "Removing port mapping for " + lanPort + ", " + wanPort
					+ ", " + prot + ", " + internalClient + ", " + description
					+ " This has no effect, this is a dummy device.");
			return true;
		}
		
		@Override
		public String getDescription() {
			return description;
		}

		@Override
		public InetAddress getInternalClient() {
			return internalClient;
		}

		@Override
		public int getLanPort() {
			return lanPort;
		}

		@Override
		public Protocol getProtocol() {
			return prot;
		}

		@Override
		public int getWanPort() {
			return wanPort;
		}
	}

	@Override
	public IPortMappingEntry getPortMapping(int extPort, Protocol prot, ILog log)
			throws PortMappingException {
		
		try {
			InetAddress addr1 = InetAddress.getByName("192.168.0.1");
			return new PortMappingEntryImpl("Descr1", addr1, extPort, prot, extPort);
		} catch (UnknownHostException e) {
			//Can not happen
			return null;
		}

	}

	@Override
	public String getPresentationURL() {
		return "http://www.bla.de";
	};
	
}
