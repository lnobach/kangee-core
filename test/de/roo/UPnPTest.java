package de.roo;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

/**
 * 
 * @author Leo Nobach (dev@getkangee.com)
 *
 */
public class UPnPTest {

	public static void main(String[] args) {
		
		try {
			test();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	public static void test() throws IOException {
		
		System.out.println("Starte...");
		
		final DatagramSocket s = new DatagramSocket(12121);
		
		new Thread() {
			public void run() {
				while(true) {
					try {
						final int RESPONSE_L = 1024;
						byte[] buf = new byte[RESPONSE_L];
						DatagramPacket response = new DatagramPacket(buf, RESPONSE_L);
						s.receive(response);
						System.out.println("Response: " + new String(response.getData()));
					} catch (IOException e) {
						e.printStackTrace();
					}	
					
				}
			}	
		}.start();
		
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		System.out.println("Request.");
		
		/*
		M-SEARCH * HTTP/1.1
		SERVER: Linux/2.6.15.2 UPnP/1.0 Mediaserver/1.0
		CACHE-CONTROL: max-age=1800
		LOCATION: http://192.168.0.10:8080/description.xml
		NTS: ssdp:alive
		NT: urn:schemas-upnp-org:service:ConnectionManager:1
		USN: uuid:550e8400-e29b-11d4-a716-446655440000::urn:schemas-upnp-org:service:ConnectionManager:1
		HOST: 239.255.255.250:1900
		
		M-SEARCH * HTTP/1.1\r\n
		HOST: 239.255.255.250:1900\r\n
		MAN: "ssdp:discover"\r\n
		MX: 3\r\n
		ST: urn:schemas-upnp- org:device:InternetGatewayDevice:1\r\n
		\r\n
		*/
		
		InetAddress target = InetAddress.getByName("239.255.255.250");
		int port = 1900;
		
		String ret = "\r\n";
		
		String req = "M-SEARCH * HTTP/1.1" + ret;
		
		req += "HOST: 239.255.255.250:1900" + ret;
		req += "MAN: \"ssdp:discover\"" + ret;
		req += "MX: 3" + ret;
		req += "ST: urn:schemas-upnp-org:device:InternetGatewayDevice:1";
		req += ret;
		
		DatagramPacket p = new DatagramPacket(req.getBytes(), req.getBytes().length, target, port);
		
		s.send(p);
		

	}
	
	public static class ListenThread extends Thread {
		
		
		
			
	}
	
}
