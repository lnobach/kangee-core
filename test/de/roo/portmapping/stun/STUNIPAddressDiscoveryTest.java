package de.roo.portmapping.stun;

import java.net.InetAddress;
import java.util.List;

import de.roo.logging.ConsoleLog;
import de.roo.logging.ILog;

public class STUNIPAddressDiscoveryTest {

	static ILog log = new ConsoleLog();
	
	public static void main(String[] args) {
		try {
			testAll();
			//testRandomSingle();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void testAll() throws Exception {

		STUNIPAddressDiscovery dscv = new STUNIPAddressDiscovery(
				STUNIPAddressDiscovery.class.getResourceAsStream("stunservers"),
				log);
		List<InetAddress> addrs = dscv.testDiscoveryOnAllServers(log);

		System.out.println("Finished. The addresses were: " + addrs);
	}
	
	public static void testRandomSingle() throws Exception {;

		STUNIPAddressDiscovery dscv = new STUNIPAddressDiscovery(
				STUNIPAddressDiscovery.class.getResourceAsStream("stunservers"),
				log);
		InetAddress addr = dscv.discover(log);

		System.out.println("Finished. The address was: " + addr);
	}

}
