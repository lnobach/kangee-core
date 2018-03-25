package de.roo.portmapping.upnp;

import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import de.roo.logging.ILog;
import de.roo.portmapping.IPortMappingDevice;
import de.roo.portmapping.IPortMappingEntry;
import de.roo.portmapping.PortMappingException;
import de.roo.util.Tuple;


/**
 * 
 * @author Leo Nobach
 *
 */
public class UPnPPortMappingDevice extends AbstractUPNPDevice implements IPortMappingDevice {

	public static final String SERVICE_URN_PPP = "urn:schemas-upnp-org:service:WANPPPConnection:1"; //Two service types are there, sometimes only one works.
	public static final String SERVICE_URN_IP = "urn:schemas-upnp-org:service:WANIPConnection:1";
	
	String serviceURN;
	
	private String deviceName;
	private String udn;
	//private String serviceID;

	//private String eventSubURL;
	private String scpdURL;
	private String presentationURL;

	public UPnPPortMappingDevice(URL serverURL, String deviceName, String udn,
			String serviceID, String controlURL, String eventSubURL,
			String scpdURL, String serviceURN, String presentationURL) {
		super(serverURL, controlURL);
		this.deviceName = deviceName;
		this.udn = udn;
		//this.serviceID = serviceID;
		//this.eventSubURL = eventSubURL;
		this.scpdURL = scpdURL;
		this.serviceURN = serviceURN;
		this.presentationURL = presentationURL;
	}

	public String toString() {
		return "UPNPPortMappingDevice[" + deviceName + ", scpd=" + scpdURL
				+ "]";
	}	
	
	public InetAddress getExternalIPAddress(ILog log) throws PortMappingException {
		
		log.dbg(this, "Getting external IP address from device " + this);

		List<Tuple<String, String>> args = Collections.emptyList();
		Map<String, String> response = doAction("GetExternalIPAddress", args, log);
		
		String extAddrFld = "NewExternalIPAddress";
		String addr = response.get(extAddrFld);
		if (addr == null) throw new PortMappingException("The response contained no field named '" + extAddrFld + "'");
		
		try {
			return InetAddress.getByName(addr);
		} catch (UnknownHostException e) {
			throw new PortMappingException("The IP address '" + addr + "' that was returned could not be parsed.", e);
		}
		
	}
	

	@Override
	public IPortMappingEntry getPortMapping(int extPort, Protocol prot, ILog log)
			throws PortMappingException {
		return getSpecificPortMappingEntry(extPort, prot, log);
	}
	
	public void addPortMapping(int lanPort, int wanPort, Protocol prot,
			InetAddress internalClient, String description, int leaseDuration, boolean enabled, ILog log)
			throws PortMappingException {
		
		log.dbg(this, "Mapping port on UPnP device: " + lanPort + ", " + wanPort + ", " + prot + ", " + internalClient + ", " + description);

		List<Tuple<String, String>> args = new ArrayList<Tuple<String, String>>(10);

		args.add(new Tuple<String, String>("NewRemoteHost", ""));
		args.add(new Tuple<String, String>("NewExternalPort", String.valueOf(wanPort)));
		args.add(new Tuple<String, String>("NewProtocol", prot.toString()));
		args.add(new Tuple<String, String>("NewInternalPort", String.valueOf(lanPort)));
		args.add(new Tuple<String, String>("NewInternalClient", internalClient.getHostAddress()));
		args.add(new Tuple<String, String>("NewEnabled", enabled?"1":"0"));
		args.add(new Tuple<String, String>("NewPortMappingDescription", description));
		args.add(new Tuple<String, String>("NewLeaseDuration", String.valueOf(leaseDuration)));
		
		doAction("AddPortMapping", args, log);
	}
	

	@Override
	public void deletePortMapping(int wanPort, Protocol prot, ILog log) throws PortMappingException {

		List<Tuple<String, String>> args = new ArrayList<Tuple<String, String>>(10);

		args.add(new Tuple<String, String>("NewRemoteHost", ""));
		args.add(new Tuple<String, String>("NewExternalPort", String.valueOf(wanPort)));
		args.add(new Tuple<String, String>("NewProtocol", prot.toString()));
		
		doAction("DeletePortMapping", args, log);
	}
	
	@Override
	public List<IPortMappingEntry> getPortMappingEntryList(int listLimit, ILog log) throws PortMappingException {
		List<IPortMappingEntry> l = new ArrayList<IPortMappingEntry>(listLimit);
		for (int i = 0; i < listLimit; i++) {
			try {
				l.add(getGenericPortMappingEntry(i, log));
			} catch (ArrayIndexOutOfBoundsException e) {
				break;
			}
		}
		return l;
	}
	
	public PortMappingEntryImpl getGenericPortMappingEntry(int index, ILog log) throws PortMappingException {
		log.dbg(this, "Requesting generic port mapping entry on index " + index);
		List<Tuple<String, String>> args = new ArrayList<Tuple<String, String>>(1);
		args.add(new Tuple<String, String>("NewPortMappingIndex", String.valueOf(index)));
		Map<String, String> respVars = doAction("GetGenericPortMappingEntry", args, log);
		return getPortMappingEntryFromUPnPAnswer(respVars);
	}
	
	public IPortMappingEntry getSpecificPortMappingEntry(int wanPort, Protocol prot, ILog log) throws PortMappingException {
		List<Tuple<String, String>> args = new ArrayList<Tuple<String, String>>(10);
		args.add(new Tuple<String, String>("NewRemoteHost", ""));
		args.add(new Tuple<String, String>("NewExternalPort", String.valueOf(wanPort)));
		args.add(new Tuple<String, String>("NewProtocol", prot.toString()));
		try {
			Map<String, String> respVars = doAction("GetSpecificPortMappingEntry", args, log);
			respVars.putAll(Tuple.getMapFromTupleList(args));
			return getPortMappingEntryFromUPnPAnswer(respVars);
		} catch (SOAPErrorException e) {
			if (e.getErrorCode() == 714) {
				//NoSuchEntryInArrayjava
				return null;
			}
			throw e;
		}
	}
	
	PortMappingEntryImpl getPortMappingEntryFromUPnPAnswer(Map<String, String> respVars) throws PortMappingException {
		String extPortStr = respVars.get("NewExternalPort");
		int extPort;
		try {
			extPort = Integer.parseInt(extPortStr);
		} catch (NumberFormatException e) {
			throw new PortMappingException("Could not read external port as an integer: " + extPortStr, e);
		}
		
		String protStr = respVars.get("NewProtocol");
		Protocol prot;
		try {
			prot = Protocol.valueOf(protStr);
		} catch (IllegalArgumentException e) {
			throw new PortMappingException("The given protocol is not 'TCP' or 'UDP': " + protStr);
		} catch (NullPointerException e) {
			throw new PortMappingException("No protocol was given");
		}
		
		String intPortStr = respVars.get("NewInternalPort");
		int intPort;
		try {
			intPort = Integer.parseInt(intPortStr);
		} catch (NumberFormatException e) {
			throw new PortMappingException("Could not read internal port as an integer: " + intPortStr, e);
		}
		
		String intClientStr = respVars.get("NewInternalClient");
		InetAddress intClient;
		try {
			intClient = InetAddress.getByName(intClientStr);
		} catch (UnknownHostException e) {
			throw new PortMappingException("The given internal client '" + intClientStr + "' could not be resolved or parsed as an IP address");
		}
		
		String enabledStr = respVars.get("NewEnabled");
		if (enabledStr == null) throw new PortMappingException("The given state (enabled or disabled) was not submitted.");
		boolean enabled = "1".equals(enabledStr.trim());
		
		String pMappingDescr = respVars.get("NewPortMappingDescription");
		
		String leaseDurStr = respVars.get("NewLeaseDuration");
		int leaseDur;
		try {
			leaseDur = Integer.parseInt(leaseDurStr);
		} catch (NumberFormatException e) {
			throw new PortMappingException("Could not read lease duration as an integer: " + leaseDurStr, e);
		}
		return new PortMappingEntryImpl(intPort, extPort, prot, intClient, pMappingDescr, enabled, leaseDur);
	}

	@Override
	public String getID() {
		return "upnp:" + udn;
	}

	@Override
	public String getName() {
		return deviceName;
	}

	@Override
	public String getTypeName() {
		return "UPnP";
	}
	
	class PortMappingEntryImpl implements IPortMappingEntry {

		private int lanPort;
		private int wanPort;
		private Protocol prot;
		private InetAddress internalClient;
		private String description;
		private boolean enabled;
		private int leaseDur;

		public PortMappingEntryImpl(int lanPort, int wanPort, Protocol prot, InetAddress internalClient, String description, boolean enabled, int leaseDur) {
			this.lanPort = lanPort;
			this.wanPort = wanPort;
			this.prot = prot;
			this.internalClient = internalClient;
			this.description = description;
			this.enabled = enabled;
			this.leaseDur = leaseDur;
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
		
		public boolean isEnabled() {
			return enabled;
		}
		
		public int getLeaseDuration() {
			return leaseDur;
		}

		@Override
		public boolean removePortMapping(ILog log) throws PortMappingException {
			log.dbg(this, "Removing port mapping for " + lanPort + ", " + wanPort
					+ ", " + prot + ", " + internalClient + ", " + description);
			deletePortMapping(wanPort, prot, log);
			return true;
		}
		
	}

	@Override
	public IPortMappingEntry forwardPort(int lanPort, int wanPort,
			Protocol prot, InetAddress internalClient, String description,
			ILog log) throws PortMappingException {
		addPortMapping(lanPort, wanPort, prot, internalClient, description, 0, true, log);
		return new PortMappingEntryImpl(lanPort, wanPort, prot, internalClient, description, true, 0);
	}

	@Override
	protected String getServiceURN() {
		return serviceURN;
	}

	@Override
	public String getPresentationURL() {
		return presentationURL;
	}
	
}
