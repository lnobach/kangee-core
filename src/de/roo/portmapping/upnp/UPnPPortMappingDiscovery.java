package de.roo.portmapping.upnp;

import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.roo.logging.ILog;
import de.roo.portmapping.IDiscoveryMechanism;
import de.roo.portmapping.IPortMappingDevice;
import de.roo.portmapping.PortMappingException;
import de.roo.util.Tuple;

/**
 * 
 * @author Leo Nobach
 *
 */
public class UPnPPortMappingDiscovery extends AbstractUPNPDiscovery implements IDiscoveryMechanism  {

	static final String DEVICE_TYPE_TO_QUERY = "urn:schemas-upnp-org:device:InternetGatewayDevice:1";
	static final List<String> SERVICE_TYPES_TO_QUERY = Arrays.asList(new String[]{UPnPPortMappingDevice.SERVICE_URN_PPP, UPnPPortMappingDevice.SERVICE_URN_IP});
	static List<InetAddress> invalidAddrs;
	
	static {
		try {
			invalidAddrs = Arrays.asList(new InetAddress[]{InetAddress.getByAddress(new byte[]{0,0,0,0})});
		} catch (UnknownHostException e) {
			invalidAddrs = Collections.emptyList();
			//May not happen.
		}
	}
	
	@Override
	public List<IPortMappingDevice> discoverPortMappingDevices(final ILog log)
			throws PortMappingException {
		final Map<String, DevTuple> devsFound = new HashMap<String, DevTuple>();
		discoverDevicesAndServices(log, DEVICE_TYPE_TO_QUERY, SERVICE_TYPES_TO_QUERY, new IFoundServiceHandler() {

			@Override
			public void foundService(URL serverURL, String deviceName,
					String udn, String serviceID, String controlURL,
					String eventSubURL, String scpdURL, String serviceType, String presentationURL) {
				UPnPPortMappingDevice dev = new UPnPPortMappingDevice(serverURL, deviceName, udn, serviceID, controlURL, eventSubURL, scpdURL, serviceType, presentationURL);
				
				DevTuple servsForUDN = devsFound.get(udn);
				if (servsForUDN == null) {
					if (UPnPPortMappingDevice.SERVICE_URN_PPP.equals(serviceType)) {
						servsForUDN = new DevTuple(dev, null);
					} else {
						servsForUDN = new DevTuple(null, dev);
					}
					devsFound.put(udn, servsForUDN);
				} else {
					if (UPnPPortMappingDevice.SERVICE_URN_PPP.equals(serviceType)) {
						if (servsForUDN.getA() != null) warnMultiDevs(log, deviceName, serviceType);
						else servsForUDN.setA(dev);
					} else {
						if (servsForUDN.getB() != null) warnMultiDevs(log, deviceName, serviceType);
						else servsForUDN.setB(dev);
					}
				}
			}

			private void warnMultiDevs(ILog log, String deviceName, String serviceType) {
				log.warn(this, "Device " + deviceName + " has multiple services of type" + serviceType + ", dropping further ones.");
			}
			
		});
		return mediateDevices(devsFound.values(), log);
	}	

	private List<IPortMappingDevice> mediateDevices(Collection<DevTuple> devsFound, ILog log) {
		List<IPortMappingDevice> result = new ArrayList<IPortMappingDevice>(devsFound.size());
		for (DevTuple dev : devsFound) {
			if (dev.getA() == null) {
				log.dbg(this, "Found port mapping device with only a WANPPPConnection service.");
				result.add(dev.getB());
			} else if (dev.getB() == null) {
				log.dbg(this, "Found port mapping device with only a WANIPConnection service.");
				result.add(dev.getA());
			} else {
				IPortMappingDevice bestDev = findBestDevice(dev.getA(), dev.getB(), log);
				log.dbg(this, "Found port mapping device with a WANPPPConnection and a WANIPConnection device, chose " + dev);
				result.add(bestDev);
			}
		}
		return result;
	}

	private IPortMappingDevice findBestDevice(UPnPPortMappingDevice a,
			UPnPPortMappingDevice b, ILog log) {
		if (worksValid(a, log)) return a;
		else return b;
	}

	private boolean worksValid(UPnPPortMappingDevice a, ILog log) {
		try {
			InetAddress addr = a.getExternalIPAddress(log);
			return !invalidAddrs.contains(addr);
		} catch (PortMappingException e) {
			log.warn(this, "Can not resolve address from device " + a, e);
			return false;
		}
	}

	@Override
	public String getTypeName() {
		return "UPnP";
	}
	
	class DevTuple extends Tuple<UPnPPortMappingDevice, UPnPPortMappingDevice> {

		public DevTuple(UPnPPortMappingDevice a, UPnPPortMappingDevice b) {
			super(a, b);
		}
		
	}


}
