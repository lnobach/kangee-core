package de.roo.util;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.regex.Pattern;

/**
 * 
 * @author Leo Nobach (dev@getkangee.com)
 *
 */
public class InetAddressToolkitTest {

	static Pattern p = Pattern.compile("(?:(?:\\[([^\\]]*)\\])|([^:\\[]+))(?::(\\d+))?");
	
	public static void main(String[] args) {
		
		testGetSockAddrFromString();
		testUniqueLocalIPv6Addr();
	}


	private static void testUniqueLocalIPv6Addr() {
		try {
			testUniqueLocalIPv6Addr(InetAddress.getByName("fb70::218:deff:feb0:d959"));
			testUniqueLocalIPv6Addr(InetAddress.getByName("fc70::218:deff:feb0:d967"));
			testUniqueLocalIPv6Addr(InetAddress.getByName("fd80::218:deff:feb0:d972"));
			testUniqueLocalIPv6Addr(InetAddress.getByName("fe90::218:deff:feb0:d959"));
		} catch (UnknownHostException e) {
			throw new RuntimeException(e);
		}
	}
	
	private static void testUniqueLocalIPv6Addr(InetAddress addr) {
		if (!(addr instanceof Inet6Address)) throw new IllegalArgumentException();
		Inet6Address addr6 = (Inet6Address)addr;
		System.out.println(addr6 + ":\t" + InetAddressToolkit.isUniqueLocalAddress(addr6));
	}


	private static void testGetSockAddrFromString() {
		testGetSockAddrFromString("ABCDE");
		testGetSockAddrFromString("ABCDE:1000");
		testGetSockAddrFromString("ABCDE:1002");
		
		testGetSockAddrFromString("bla.test.de");
		testGetSockAddrFromString("bla.test.de:8080");
		
		testGetSockAddrFromString("relevantmusic.de");
		testGetSockAddrFromString("google.de:8080");
		
		testGetSockAddrFromString("[::1]");
		testGetSockAddrFromString("[::1]:");
		testGetSockAddrFromString("[::1]:22");
		testGetSockAddrFromString("[::1f:22:1]:2040");
	}
	
	
	public static void testGetSockAddrFromString(String testStr) {
		try {
			System.out.println(InetAddressToolkit.getSocketAddressFromString(testStr, 80));
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
	}
	
	
}
