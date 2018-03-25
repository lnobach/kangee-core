package de.roo.connectivity;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

import de.roo.configuration.IConf;
import de.roo.logging.ILog;
import de.roo.util.InetAddressToolkit;
import de.roo.util.Tuple;

/**
 * 
 * @author Leo Nobach
 *
 */
public class LanAddressing {

	public static InetAddress getLanIP(ILog log, IConf conf) {
		
		InetAddress overrAddr = lookForAddressOverride(conf);
		if (overrAddr != null) return overrAddr;

		List<Tuple<InetAddress, String>> allAddrs = InetAddressToolkit.acquireAllAddrs(log);		

		InetAddress confAddr = chooseConfiguredAddr(conf, log, allAddrs);
		if (confAddr != null) return confAddr;
		
		if (conf.getValueBoolean("IPv6Mode", false)) {
			confAddr = InetAddressToolkit.getInet6Address(log, allAddrs, true);
			if (confAddr != null) return confAddr;
		}
		
		return InetAddressToolkit.chooseDefaultLanAddr(log, allAddrs);

	}

	private static InetAddress lookForAddressOverride(IConf conf) {
		String overrideLanIP = conf.getValueString("Lan_Address", "auto");
		if (!"auto".equals(overrideLanIP)) {
			try {
				return InetAddress.getByName(overrideLanIP);
			} catch (UnknownHostException e) {
				throw new IllegalArgumentException("The LAN address '"
						+ overrideLanIP
						+ " that shall be used to override is bad.", e);
			}
		}
		return null;
	}

	private static InetAddress chooseConfiguredAddr(IConf conf, ILog log,
			List<Tuple<InetAddress, String>> allAddrs) {
		
		final String SeqPrefKey = "Lan_Address_Seq_Pref";
	
		String seqPrefStr = conf.getValueString(SeqPrefKey, "auto");
	
		try {
			int seqPref = Integer.parseInt(seqPrefStr);
			Tuple<InetAddress, String> result = allAddrs.get(seqPref);
			log.dbg(LanAddressing.class, "Choosing address"
					+ result.getA() + ", " + result.getB()
					+ " as selected in configuration entry " + SeqPrefKey
					+ " as LAN address.");
			return result.getA();
		} catch (NumberFormatException e) {
			log.dbg(LanAddressing.class,
				"No preference was set for an IP address. Trying to determine the best one for you.");
		} catch (IndexOutOfBoundsException e) {
			log.error(
				LanAddressing.class,
				"Set LAN ip address preference sequence number for " + seqPrefStr
				+ ", but it is out of bounds. Trying to determine the best one for you.");
		}
		return null;
	}

	

}
