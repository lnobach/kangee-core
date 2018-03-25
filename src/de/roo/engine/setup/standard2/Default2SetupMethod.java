package de.roo.engine.setup.standard2;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.roo.BuildConstants;
import de.roo.configuration.IConf;
import de.roo.configuration.IWritableConf;
import de.roo.configuration.NoSuchCfgKeyException;
import de.roo.connectivity.ConnectivityInfo;
import de.roo.connectivity.DiscoveryException;
import de.roo.connectivity.LanAddressing;
import de.roo.connectivity.RemoteDiscovery;
import de.roo.connectivity.RemoteDiscovery.CheckConnectivityState;
import de.roo.connectivity.RemoteDiscovery.DiscoveryInfo;
import de.roo.engine.setup.ISetupContext;
import de.roo.engine.setup.ISetupFollower;
import de.roo.engine.setup.ISetupMethod;
import de.roo.engine.setup.SetupException;
import de.roo.httpsrv.Server;
import de.roo.logging.ILog;
import de.roo.portmapping.AddressDiscoveryException;
import de.roo.portmapping.IPortMappingDevice;
import de.roo.portmapping.IPortMappingEntry;
import de.roo.portmapping.PortMapping;
import de.roo.portmapping.PortMappingException;
import de.roo.portmapping.IPortMappingDevice.Protocol;
import de.roo.portmapping.stun.STUNIPAddressDiscovery;
import de.roo.srv.RooPingHandler;
import de.roo.srvApi.ServerException;
import de.roo.util.InetAddressToolkit;
import de.roo.util.StringToolkit;
import de.roo.util.Tuple;

/**
 * 
 * @author Leo Nobach
 *
 */
public class Default2SetupMethod implements ISetupMethod {

	@Override
	public void setup(ISetupContext ctx, ISetupFollower follower)
			throws SetupException {
		
		IWritableConf conf = ctx.getConf();
		ILog log = ctx.getLog();
		if (!conf.getValueBoolean("configured", false)) throw new SetupException.NotConfigured();
		
		int port = conf.getValueInt("Port", 8080);
		InetAddress wanAddr = null;
		
		InetAddress lanAddr = getLanIP(log, conf);
		
		String presentationURLSeen = null;
		
		wanAddr = getFixedAddr(ctx);
		if (wanAddr != null) {
			log.dbg(this, "There is a fixed address " + wanAddr + ", using it.");
			proceedConnectionTest(port, ctx, lanAddr, wanAddr, follower, presentationURLSeen);
			return;
		}
		log.dbg(this, "There is no fixed address, proceeding...");
		if (conf.getValueBoolean("ConnPMapQueryEnabled", true)) {
			try {
				follower.setCurrentJob("Retrieving port mapping devices...");
				Set<String> cachedDevs = getLimitedPortmappingDevices(conf);
				List<IPortMappingDevice> devs = PortMapping.discoverAllDevices(log);
				if (cachedDevs.isEmpty()) {
					log.dbg(this, "Port mapping is not restricted, will try it on all devices found: '" + printAllPMapDevIDsFound(devs) + "'");
				} else log.dbg(this, "Port mapping is restricted to the devices '" + StringToolkit.merge(cachedDevs, ";") + "'");
				boolean portMappingEnabled = conf.getValueBoolean("ConnPMappingEnabled", true);
				if (!devs.isEmpty()) follower.setCurrentJob("Mapping ports...");
				for (IPortMappingDevice dev : devs) {
					try {
						log.dbg(this, "Trying to retrieve WAN address from device " + dev);
						wanAddr = dev.getExternalIPAddress(log);
						presentationURLSeen = dev.getPresentationURL();
						//log.warn(this, "Presentation URL seen: " + presentationURLSeen);
						if (portMappingEnabled && (cachedDevs.isEmpty() || cachedDevs.contains(dev.getID()))) {
							IPortMappingEntry pmap;
							if (conf.getValueBoolean("ConnPMapAutoIncrement", true)) {
								log.dbg(this, "Trying to map ports on device " + dev + ", automatically incrementing.");
								pmap = PortMapping.forwardAutoIncrement(dev, log, port, Protocol.TCP, 
										"Done by " + BuildConstants.PROD_TINY_NAME_VER, lanAddr, 
										conf.getValueInt("ConnPMapForwardingIncrementLimit", 20));
							} else {
								log.dbg(this, "Trying to map ports on device " + dev + ", not automatically incrementing.");
								pmap = dev.forwardPort(port, port, Protocol.TCP, lanAddr, 
										"Done by " + BuildConstants.PROD_TINY_NAME_VER, log);
							}
							port = pmap.getWanPort();
							ctx.addUnsetup(new PortMappingUnsetup(pmap, conf));
							break;
						} else if (!portMappingEnabled) {
							break;	//Address is there, so quit loop.
						}
					} catch (PortMappingException e) {
						log.warn(this, "Error while mapping port on device " + dev, e);
					}
				}
				if (wanAddr != null) {
					log.dbg(this, "We have the WAN address, so proceed.");
					proceedConnectionTest(port, ctx, lanAddr, wanAddr, follower, presentationURLSeen);
					return;
				}
			} catch (PortMappingException e) {
				log.error(this, "Error while discovering port mapping devices. ", e);
			}
		}
		
		
		if (proceedConnectionTest(port, ctx, lanAddr, null, follower, presentationURLSeen)) return;
		
		if (!conf.getValueBoolean("ConnForceSTUNOverAdapters", false)) {
			log.dbg(this, "Trying to determine a WAN address in the adapters.");
			wanAddr = determineWANInAdapters(log, conf);
			if (wanAddr != null) {
				log.dbg(this, "Found WAN address " + wanAddr + " in adapters");
				ctx.setConnectivityInfo(new ConnectivityInfo(lanAddr, wanAddr, port));
				return;
			}
		}
		
		if (conf.getValueBoolean("ConnSTUNEnabled", true)) {
			try {
				follower.setCurrentJob("Discovering external address via STUN...");
				STUNIPAddressDiscovery dscv = new STUNIPAddressDiscovery(getSTUNServers(conf, log), log);
				wanAddr = dscv.discoverSuccessiveRetry(log);
				log.dbg(this, "Found a WAN address via STUN: " + wanAddr);
				ctx.setConnectivityInfo(new ConnectivityInfo(lanAddr, wanAddr, port));
				return;
			} catch (IOException e) {
				log.error(this, "STUN IP address discovery failed.", e);
			} catch (AddressDiscoveryException e) {
				log.error(this, "STUN IP address discovery failed.", e);
			}
		}
		try {
			ctx.setConnectivityInfo(new ConnectivityInfo(lanAddr, InetAddress.getLocalHost(), port));
		} catch (UnknownHostException e) {
			log.error(this, "Error resolving localhost address", e);
		}
		throw new SetupException("All methods to get an external IP address failed. " + BuildConstants.PROD_TINY_NAME + " may not work properly.");
	}

	private InputStream getSTUNServers(IConf conf, ILog log) throws FileNotFoundException {
		String extSTUNSrvFl = conf.getValueString("ConnExternalSTUNServersFile", "");
		if (!"".equals(extSTUNSrvFl)) {
			File f = new File(extSTUNSrvFl);
			return new BufferedInputStream(new FileInputStream(f));
		}
		return STUNIPAddressDiscovery.class.getResourceAsStream("stunservers");
	}

	protected Set<String> getLimitedPortmappingDevices(IConf conf) {
		try {
			String collStr = conf.getValueString("PortMappingDevsToUse");
			return new HashSet<String>(StringToolkit.split(collStr, ";"));
		} catch (NoSuchCfgKeyException e) {
			return Collections.emptySet();
		}	
	}
	
	private String printAllPMapDevIDsFound(List<IPortMappingDevice> devs) {
		List<String> devIDs = new ArrayList<String>(devs.size());
		for (IPortMappingDevice dev : devs) devIDs.add(dev.getID());
		return StringToolkit.merge(devIDs, ";");
	}
	
	private InetAddress getFixedAddr(ISetupContext ctx) throws SetupException {
		IWritableConf conf = ctx.getConf();
		ILog log = ctx.getLog();
		
		String addressStr = null;
		try {
			addressStr = conf.getValueString("ConnFixedAddress").trim();
			if (addressStr == "") return null;
			log.dbg(this, "Using fixed address as WAN address: " + addressStr);
			return InetAddress.getByName(addressStr);
		} catch (NoSuchCfgKeyException e) {
			return null;
		} catch (UnknownHostException e) {
			throw new SetupException("The IP address/hostname of '" + addressStr + "', for use as the fixed address could not be resolved.");
		}
	}
	
	/**
	 * Returns whether the connection test was successful.
	 */
	protected boolean proceedConnectionTest(int port, ISetupContext ctx, InetAddress lanAddr, 
			InetAddress wanAddress, ISetupFollower flw, String presentationURLSeen) throws SetupException {
		IConf conf = ctx.getConf();
		ILog log = ctx.getLog();

		if (conf.getValueBoolean("ConnRemoteTestEnabled", true)) {
			flw.setCurrentJob("Testing connection...");
			String discoveryURL = conf.getValueString("Discovery_URL", BuildConstants.Default_Discovery_URLs[0]);
			log.dbg(this, "Doing connection test with '" + discoveryURL + "'");
			String problemStr;
			try {
				boolean reqDummyServ = ctx.isServerRunning() != port; //Only if the server is not running on the port already.
				Server srv = reqDummyServ?new Server(port, log, new RooPingHandler(log, ctx.getRooID()), conf):null;
				try {
					DiscoveryInfo info = RemoteDiscovery.discover(discoveryURL, port, true, ctx.getRooID());
					if (info.getCheckState() == CheckConnectivityState.OK) {
						ctx.setConnectivityInfo(new ConnectivityInfo(lanAddr, info.getDiscoveredAddr(), port));
						if (reqDummyServ) srv.stopServer();
						return true;
					}
					problemStr = "The test server could not reach Kangee. " + info.getCheckErrorNum() + ", " + info.getCheckErrorStr();
					if (reqDummyServ) srv.stopServer();
					ctx.setConnectivityInfo(new ConnectivityInfo(lanAddr, info.getDiscoveredAddr(), port));
					throw new SetupException.BadConnectionTest(problemStr, port, lanAddr, info.getDiscoveredAddr(), presentationURLSeen);
					
				} catch (DiscoveryException e) {
					if (reqDummyServ) srv.stopServer();
					ctx.setConnectivityInfo(new ConnectivityInfo(lanAddr, wanAddress, port));
					log.error(this, "Error contacting test server.", e);
					throw new SetupException.BadConnectionTest("Error contacting test server.", port, lanAddr, wanAddress, presentationURLSeen, e);
				}
			} catch (ServerException e) {
				ctx.setConnectivityInfo(new ConnectivityInfo(lanAddr, wanAddress, port));
				throw new SetupException.BadConnectionTest("Error setting up preliminary HTTP server locally.", port, lanAddr, wanAddress, presentationURLSeen, e);
			}
		} else {
			if (wanAddress != null) {
				ctx.setConnectivityInfo(new ConnectivityInfo(lanAddr, wanAddress, port));
				return true;
			}
			return false;
		}
	}


	private InetAddress getLanIP(ILog log, IConf conf) {
		return LanAddressing.getLanIP(log, conf);
	}
	
	
	private InetAddress determineWANInAdapters(ILog log, IConf conf) {
		List<Tuple<InetAddress, String>> addrs = InetAddressToolkit.acquireAllAddrs(log);
		if (conf.getValueBoolean("IPv6Mode", true)) return InetAddressToolkit.getInet6Address(log, addrs, false);
		for (Tuple<InetAddress, String> tpl : addrs) {
			InetAddress addr = tpl.getA();
			if (addr instanceof Inet4Address && !InetAddressToolkit.isv4LANAddress(addr) && !InetAddressToolkit.isv4LoopbackAddress(addr)) return addr;
		}
		return null;
	}

}
