package de.roo.util;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.roo.logging.ILog;

/**
 * 
 * @author Leo Nobach
 *
 */
public class InetAddressToolkit {

	static final InetAddrRange IPV4_PRIVATE_1;
	static final InetAddrRange IPV4_PRIVATE_2;
	static final InetAddrRange IPV4_PRIVATE_3;

	static final InetAddrRange IPV4_LOOPBACK;
	static final InetAddress IPV4_LOOPBACK_DEFAULT;

	static {
		try {
			IPV4_LOOPBACK_DEFAULT = InetAddress.getByName("127.0.0.1");

			IPV4_PRIVATE_1 = new InetAddrRange(
					InetAddress.getByName("10.0.0.0"),
					InetAddress.getByName("10.255.255.255"));
			IPV4_PRIVATE_2 = new InetAddrRange(
					InetAddress.getByName("172.16.0.0"),
					InetAddress.getByName("172.16.255.255"));
			IPV4_PRIVATE_3 = new InetAddrRange(
					InetAddress.getByName("192.168.0.0"),
					InetAddress.getByName("192.168.255.255"));

			IPV4_LOOPBACK = new InetAddrRange(
					InetAddress.getByName("127.0.0.0"),
					InetAddress.getByName("127.255.255.255"));

		} catch (UnknownHostException e) {
			throw new RuntimeException(e);
		}
	}
	
	public static boolean isv4LANAddress(InetAddress addr) {
		if (!(addr instanceof Inet4Address)) return false;
		return IPV4_PRIVATE_1.contains(addr) || IPV4_PRIVATE_2.contains(addr)
				|| IPV4_PRIVATE_3.contains(addr);
	}

	public static boolean isv4LoopbackAddress(InetAddress addr) {
		if (!(addr instanceof Inet4Address)) return false;
		return IPV4_LOOPBACK.contains(addr);
	}

	public static final Comparator<InetAddress> INET_ADDR_COMP = new Comparator<InetAddress>() {

		@Override
		public int compare(InetAddress o1, InetAddress o2) {

			if (o1 instanceof Inet4Address != o2 instanceof Inet4Address)
				throw new IllegalArgumentException(
						"Not comparable: o1 and o2 are neither both IPv4 addresses nor both IPv6 addresses");

			byte[] addr1 = o1.getAddress();
			byte[] addr2 = o2.getAddress();

			for (int i = 0; i < addr1.length; i++) {
				if (u(addr1[i]) < u(addr2[i])) {
					// System.out.println(u(addr1[i]) + " < " + u(addr2[i]));
					return -1;
				}
				if (u(addr1[i]) > u(addr2[i])) {
					// System.out.println(u(addr1[i]) + " > " + u(addr2[i]));
					return 1;
				}
			}

			return 0;

		}

		private int u(byte b) {
			return (int) b & 0xFF;
		}

	};

	public static class InetAddrRange {

		public InetAddress lower;
		public InetAddress upper;

		public InetAddrRange(InetAddress lower, InetAddress upper) {
			super();
			this.lower = lower;
			this.upper = upper;
		}

		public InetAddress getLower() {
			return lower;
		}

		public void setLower(InetAddress lower) {
			this.lower = lower;
		}

		public InetAddress getUpper() {
			return upper;
		}

		public void setUpper(InetAddress upper) {
			this.upper = upper;
		}

		public boolean contains(InetAddress addr) {
			return INET_ADDR_COMP.compare(lower, addr) <= 0
					&& INET_ADDR_COMP.compare(upper, addr) >= 0;
		}

	}

	public static InetAddress getLoopbackAddress() {
		return IPV4_LOOPBACK_DEFAULT;
	}

	public static InetAddress getDefaultLanAddr(ILog log) {
		List<Tuple<InetAddress, String>> allAddrs = acquireAllAddrs(log);
		return chooseDefaultLanAddr(log, allAddrs);
	}

	public static InetAddress chooseDefaultLanAddr(ILog log,
			List<Tuple<InetAddress, String>> allAddrs) {
		Tuple<InetAddress, String> addr = determineFirstLANIPv4Found(allAddrs);
		if (addr != null) {
			log.dbg(InetAddressToolkit.class,
					"Taking LAN address " + addr.getA() + ", " + addr.getB());
			return addr.getA();
		}
		addr = determineFirstNonLoopbackIPv4Found(allAddrs);
		if (addr != null) {
			log.dbg(InetAddressToolkit.class,
					"No LAN address found, but found a good WAN address instead: "
							+ addr.getA() + ", " + addr.getB());
			return addr.getA();
		}
		log.error(InetAddressToolkit.class,
				"The operating system returns no LAN adress. Giving the loopback address back.");
		return InetAddressToolkit.getLoopbackAddress();
	}

	private static Tuple<InetAddress, String> determineFirstNonLoopbackIPv4Found(
			List<Tuple<InetAddress, String>> allAddrs) {
		for (Tuple<InetAddress, String> t : allAddrs) {
			InetAddress a = t.getA();
			if (a instanceof Inet4Address
					&& !InetAddressToolkit.isv4LoopbackAddress(a))
				return t;
		}
		return null;
	}

	protected static Tuple<InetAddress, String> determineFirstLANIPv4Found(
			List<Tuple<InetAddress, String>> allAddrs) {
		for (Tuple<InetAddress, String> t : allAddrs) {
			InetAddress a = t.getA();
			if (a instanceof Inet4Address && InetAddressToolkit.isv4LANAddress(a))
				return t;
		}
		return null;
	}

	public static List<Tuple<InetAddress, String>> acquireAllAddrs(ILog log) {
		List<Tuple<InetAddress, String>> lanAddrs = new LinkedList<Tuple<InetAddress, String>>();

		try {
			lanAddrs.add(new Tuple<InetAddress, String>(InetAddress
					.getLocalHost(), "acquired via InetAddress.getLocalHost()"));
		} catch (UnknownHostException e) {
			log.warn(
					InetAddressToolkit.class,
					"UnknownHostException while trying to acquire LAN ip address via InetAddress.getLocalHost()", e);
		}

		try {
			Enumeration<NetworkInterface> e = NetworkInterface
					.getNetworkInterfaces();
			while (e.hasMoreElements()) {
				NetworkInterface ni = (NetworkInterface) e.nextElement();
				Enumeration<InetAddress> e2 = ni.getInetAddresses();
				while (e2.hasMoreElements()) {
					InetAddress ip = (InetAddress) e2.nextElement();
					lanAddrs.add(new Tuple<InetAddress, String>(ip,
							"found on net interface " + ni.getName()));
				}
			}
		} catch (SocketException e) {
			log.error(
					InetAddressToolkit.class,
					"Socket exception while trying to acquire LAN IP address on device lists.",
					e);
		}

		int seqC = 0;
		for (Tuple<InetAddress, String> addr : lanAddrs) {
			log.dbg(InetAddressToolkit.class, "Found address (seqPref=" + seqC
					+ ") " + addr.getA() + ", " + addr.getB());
			seqC++;
		}

		return lanAddrs;
	}

	static Pattern socketAddressP = Pattern
			.compile("(?:(?:\\[([^\\]]*)\\])|([^:\\[]+))(?::(\\d+))?");

	public static InetSocketAddress getSocketAddressFromString(String str, int defaultPort)
			throws IllegalArgumentException, UnknownHostException {
		Matcher m = socketAddressP.matcher(str);
		if (m.find()) {
			String host = m.group(1);	//test if we have found an IPv6 address in brackets.
			if (host == null)	//it is not an IPv6 address in brackets.
				host = m.group(2);
			String portStr = m.group(3);
			int port;
			if (portStr == null) port = defaultPort;
			else {
				try {
					port = Integer.parseInt(portStr);
				} catch (NumberFormatException e) {
					throw new IllegalArgumentException("Illegal port number.", e);
				}
			}
			return InetSocketAddress.createUnresolved(host, port);
		} else {
			throw new IllegalArgumentException(
					"Illegal internet socket syntax: " + str);
		}
	}
	
	public static InetAddress getInet6Address(ILog log, List<Tuple<InetAddress, String>> allAddrs, boolean preferLocal) {
		for (Tuple<InetAddress, String> tpl : allAddrs) {
			if (tpl.getA() instanceof Inet6Address) {
				Inet6Address addr6 = (Inet6Address)tpl.getA();
				boolean isLocal = addr6.isLinkLocalAddress() || isUniqueLocalAddress(addr6) || addr6.isSiteLocalAddress();
				if (preferLocal && isLocal) return addr6;
				if (!preferLocal && !isLocal && !addr6.isLoopbackAddress() && !addr6.isMulticastAddress()) return addr6;
			}
		}
		for (Tuple<InetAddress, String> tpl : allAddrs) {
			if (tpl.getA() instanceof Inet6Address) {
				Inet6Address addr6 = (Inet6Address)tpl.getA();
				if (!addr6.isLoopbackAddress() && !addr6.isMulticastAddress()) return addr6;
			}
		}
		return null;
	}
    
    public static boolean isUniqueLocalAddress(Inet6Address addr) {
    	byte[] ipaddress = addr.getAddress();
        return (ipaddress[0] & 0xfe) == 0xfc;
    }

}
