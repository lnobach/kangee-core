package de.roo.portmapping.upnp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.SocketException;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import de.roo.logging.ILog;
import de.roo.portmapping.PortMappingException;

/**
 * 
 * @author Leo Nobach
 *
 */
public abstract class AbstractUPNPDiscovery{
	
	static final boolean DBG_PACKETS = true;

	public AbstractUPNPDiscovery() {
	}

	public void discoverDevicesAndServices(ILog log, String deviceTypeToQuery, List<String> serviceURNsToQuery, IFoundServiceHandler hdlr)
			throws PortMappingException {
		
		List<String> responses = doSSDPDiscovery(log, deviceTypeToQuery);
		for (String response : responses)
			try {
				handleSSDPResponses(response, log, serviceURNsToQuery, hdlr);
			} catch (SSDPException e) {
				log.error(this, "Could not handle SSDP response." + e);
			}
		
	}

	private List<String> doSSDPDiscovery(ILog log, String deviceTypeToQuery) throws PortMappingException {
		try {
			final DatagramSocket s = new DatagramSocket();
			InetAddress target = InetAddress.getByName("239.255.255.250");
			int port = 1900;
			int timeout = 1000; // msec

			UDPReceiverThread rcv = new UDPReceiverThread(s);
			rcv.start();

			String ret = "\r\n";

			String req = "M-SEARCH * HTTP/1.1" + ret;

			req += "HOST: 239.255.255.250:1900" + ret;
			req += "MAN: \"ssdp:discover\"" + ret;
			req += "MX: 3" + ret;
			req += "ST: " + deviceTypeToQuery + ret;
			req += ret;

			DatagramPacket p = new DatagramPacket(req.getBytes(), req
					.getBytes().length, target, port);

			log.dbg(this, "Sending M-SEARCH SSDP discovery packet.");
			
			if (DBG_PACKETS) {
				log.dbg(this, "===== SSDP Request to MCast IP " + target + ": =====\n " + req);
			}
			
			s.send(p);

			log.dbg(this, "Waiting for " + timeout + " seconds for responses to come back...");
			try {
				Thread.sleep(timeout);
			} catch (InterruptedException e) {
				// Nothing to do, wayne
			}

			rcv.terminate();
			return rcv.getResponses();

		} catch (SocketException e) {
			throw new PortMappingException("Could not use socket.", e);
		} catch (UnknownHostException e) {
			throw new PortMappingException(e);
		} catch (IOException e) {
			throw new PortMappingException(
					"I/O Error while sending and waiting for packet.", e);
		}
	}

	private void handleSSDPResponses(String response, ILog log, List<String> serviceURNsToQuery, IFoundServiceHandler hdlr)
			throws SSDPException {
		
		if (DBG_PACKETS) log.dbg(this, "====RESPONSE====\n" + response);
		
		String[] lines = response.split("\r?\n");
		
		String statusLine = lines[0];
		checkSSDPStatusLine(statusLine);
		
		Map<String, String> respVals = parseSSDPHeaders(lines, log);
		
		String serverName = respVals.get("server");
		log.dbg(this, "Found UPnP server: " + serverName);
		
		String usn = respVals.get("usn");
		
		String descrLocation = respVals.get("location");
		if (descrLocation == null) throw new SSDPException("Server gave no SSDP description location.");
		
		handleSSDPDescription(serverName, usn, descrLocation, log, serviceURNsToQuery, hdlr);
		
	}

	private void checkSSDPStatusLine(String statusLine) throws SSDPException {
		String[] tokens = statusLine.split(" ");
		if (tokens.length < 3) throw new SSDPException("Response status line has not enough elements: " + statusLine);
		if (!tokens[0].startsWith("HTTP")) throw new SSDPException("No HTTP response: " + statusLine);
		if (!tokens[1].equals("200")) throw new SSDPException("SSDP HTTP response status was not \"200 OK\": " + statusLine);
	}
	
	private Map<String, String> parseSSDPHeaders(String[] lines, ILog log) {
		Map<String, String> respVals = new HashMap<String, String>(20);
		for (int i = 1; i < lines.length; i++) {
			String line = lines[i];
			if (!line.trim().equals("")) {
				int splitPos = line.indexOf(": ");
				if (splitPos < 0) {
					if (line.endsWith(":")) respVals.put(line.substring(0, line.length()-1), "");
					else log.error(this, "Response line malformed: " + line);
				} else {
					respVals.put(line.substring(0, splitPos).toLowerCase(), line.substring(splitPos +2));
				}
			}
			
		}
		return respVals;
	}

	private void handleSSDPDescription(String serverName,
			String usn, String descrLocation, ILog log, List<String> serviceURNsToQuery, IFoundServiceHandler hdlr) throws SSDPException {
		
		try {
			URL url = new URL(descrLocation);
			URLConnection conn = url.openConnection();
			
			DocumentBuilder b = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			Document doc = b.parse(conn.getInputStream());
			
			doc.getDocumentElement().normalize();
			
			NodeList children = doc.getDocumentElement().getChildNodes();
			
			for (int i = 0; i < children.getLength(); i++) {
				Node n = children.item(i);
				if (n.getNodeType() == Node.ELEMENT_NODE) {
					//System.out.println(n.getNodeName() + " " + n.getNodeType());
					if ("device".equalsIgnoreCase(n.getNodeName())) {
						handleSSDPXMLDevice(url, n, log, serviceURNsToQuery, hdlr, null);
					}
				}
			}
			
		} catch (MalformedURLException e) {
			throw new SSDPException("UPnP Service Discovery URL is malformed: " + descrLocation);
		} catch (ParserConfigurationException e) {
			throw new SSDPException("Problems while loading SAX parser: ", e);
		} catch (IOException e) {
			throw new SSDPException("I/O exception while reading UPnP description: ", e);
		} catch (SAXException e) {
			throw new SSDPException("Problems while executing SAX parser: ", e);
		}
		
	}

	private void handleSSDPXMLDevice(URL serverURL, Node node, ILog log, List<String> serviceURNsToQuery, IFoundServiceHandler hdlr, String parentPresentationURL) {
		
		String name = null;
		String udn = null;
		String presentationURL = parentPresentationURL;
		
		Node serviceList = null;
		Node deviceList = null;
		
		NodeList children = node.getChildNodes();
		for (int i = 0; i < children.getLength(); i++) {
			Node n = children.item(i);
			
			
			if (n.getNodeType() == Node.ELEMENT_NODE) {
				if ("friendlyName".equalsIgnoreCase(n.getNodeName())) {
					name = n.getTextContent().trim();
				} else if ("UDN".equalsIgnoreCase(n.getNodeName())) {
					udn = n.getTextContent().trim();
				} else if ("serviceList".equalsIgnoreCase(n.getNodeName())) {
					serviceList = n;
				} else if ("deviceList".equalsIgnoreCase(n.getNodeName())) {
					deviceList = n;
				} else if ("presentationURL".equalsIgnoreCase(n.getNodeName())) {
					presentationURL = n.getTextContent().trim();
				}
			}
		}
		
		log.dbg(this, "Found UPnP device: " + name);
		
		if (serviceList != null) {
			NodeList servListChildren = serviceList.getChildNodes();	
			for (int i = 0; i < servListChildren.getLength(); i++) {
				Node n = servListChildren.item(i);
				
				
				if (n.getNodeType() == Node.ELEMENT_NODE) {
					if ("service".equalsIgnoreCase(n.getNodeName())) {
						handleService(serverURL, n, name, udn, log, serviceURNsToQuery, hdlr, presentationURL);
					}
				}
			}
		}
		
		if (deviceList != null) {
			NodeList devListChildren = deviceList.getChildNodes();
			for (int i = 0; i < devListChildren.getLength(); i++) {
				Node n = devListChildren.item(i);
				
				
				if (n.getNodeType() == Node.ELEMENT_NODE) {
					if ("device".equalsIgnoreCase(n.getNodeName())) {
						handleSSDPXMLDevice(serverURL, n, log, serviceURNsToQuery, hdlr, presentationURL);
					}
				}
			}
		}
		
	}

	private void handleService(URL serverURL, Node node, String deviceName, String udn,
			ILog log, List<String> serviceURNsToQuery, IFoundServiceHandler hdlr, String presentationURL) {
		//System.out.println("handleService(" + node + ", " + deviceName + ", " + udn + ", ...)");
		
		String serviceType = null;
		
		String serviceID = null;
		String controlURL = null;
		String eventSubURL = null;
		String scpdURL = null;
		
		NodeList children = node.getChildNodes();
		for (int i = 0; i < children.getLength(); i++) {
			Node n = children.item(i);
			
			
			if (n.getNodeType() == Node.ELEMENT_NODE) {
				if ("serviceType".equalsIgnoreCase(n.getNodeName())) {
					serviceType = n.getTextContent().trim();
					if (!serviceURNsToQuery.contains(serviceType)) return;
				} else if ("serviceId".equalsIgnoreCase(n.getNodeName())) {
					serviceID = n.getTextContent().trim();
				} else if ("controlURL".equalsIgnoreCase(n.getNodeName())) {
					controlURL = n.getTextContent().trim();
				} else if ("eventSubURL".equalsIgnoreCase(n.getNodeName())) {
					eventSubURL = n.getTextContent().trim();
				} else if ("SCPDURL".equalsIgnoreCase(n.getNodeName())) {
					scpdURL = n.getTextContent().trim();
				}
				
			}
		}
		if (serviceType == null) {
			log.error(this, "Service on device " + deviceName + " has no service type");
			return;
		}
		if (serviceID == null) {
			log.error(this, "Service on device " + deviceName + " has no service ID");
			return;
		}
		
		log.dbg(this, "Found matching UPnP service for device: " + serviceID);
		
		hdlr.foundService(serverURL, deviceName, udn, serviceID, controlURL, eventSubURL, scpdURL, serviceType, presentationURL);


		
	}

	public static interface IFoundServiceHandler {
		public void foundService(URL serverURL, String deviceName, String udn,
				String serviceID, String controlURL, String eventSubURL,
				String scpdURL, String serviceType, String presentationURL);
	}
	


}















