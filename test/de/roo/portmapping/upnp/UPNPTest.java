package de.roo.portmapping.upnp;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

import de.roo.logging.ConsoleLog;
import de.roo.logging.ILog;
import de.roo.portmapping.IDiscoveryMechanism;
import de.roo.portmapping.IPortMappingDevice;
import de.roo.portmapping.PortMappingException;
import de.roo.portmapping.IPortMappingDevice.Protocol;

public class UPNPTest {

	public static void main(String[] args) {
		
		ILog log = new ConsoleLog();
		
		IDiscoveryMechanism m = new UPnPPortMappingDiscovery();
		
		try {
			List<IPortMappingDevice> devs = m.discoverPortMappingDevices(log);
			Thread.sleep(500);
			devs.get(0).forwardPort(12343, 12343, Protocol.TCP, InetAddress.getByName("192.168.0.35"), "Roo", log);
			System.out.println("Test finished successfully.");
			
		} catch (PortMappingException e) {
			e.printStackTrace();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		System.exit(0);
	}
	
}
