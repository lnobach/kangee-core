package de.roo.portmapping.stun;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import de.roo.logging.ILog;
import de.roo.portmapping.AddressDiscoveryException;
import de.roo.portmapping.IWANInetAddressDiscovery;
import de.roo.util.CollectionUtils;
import de.roo.util.InetAddressToolkit;

/**
 * 
 * @author Leo Nobach
 *
 */
public class STUNIPAddressDiscovery extends STUNRequestResponse implements IWANInetAddressDiscovery {
	
	static final int TIMEOUT = 1000;
	
	static final String DEFAULT_STUN_SERVERS_FILE = "stunservers";
	
	static final int DEFAULT_STUN_SERVER_PORT = 3478;
	
	List<InetSocketAddress> stunServers = new ArrayList<InetSocketAddress>();
	
	public STUNIPAddressDiscovery() {
		
	}
	
	public STUNIPAddressDiscovery(InputStream stunServers, ILog log) throws IOException {
		addSTUNServersFromFile(stunServers, log);
		dbgAvailableStunServers(log);
	}

	public STUNIPAddressDiscovery(List<InetSocketAddress> addrs, ILog log) {
		stunServers.addAll(addrs);
		dbgAvailableStunServers(log);
	}
	
	public void addSTUNServersFromFile(InputStream stunServersS, ILog log) throws IOException {
		stunServers.addAll(getStunServersFromStream(stunServersS, log));
	}
	
	private Collection<? extends InetSocketAddress> getStunServersFromStream(
			InputStream stunServers, ILog log) throws IOException {
		List<InetSocketAddress> addrs = new ArrayList<InetSocketAddress>(100);
		InputStreamReader r = new InputStreamReader(stunServers);
		BufferedReader reader = new BufferedReader(r);
		String line;
		while ((line = reader.readLine()) != null) {
			try {
				
				int commentDelim = line.indexOf("#");
				String lineTr;
				if (commentDelim < 0) lineTr = line.trim();
				else lineTr = line.substring(0, commentDelim).trim();
				if (!"".equals(lineTr)) addrs.add(getSTUNServerFromString(lineTr, log));
			} catch (IllegalArgumentException e) {
				log.warn(this, "Problems while parsing STUN server line " + line, e);
			} catch (UnknownHostException e) {
				log.warn(this, "Problems while resolving STUN server." + line, e);
			}
		}
		reader.close();
		r.close();
		return addrs;
	}
	
	public void dbgAvailableStunServers(ILog log) {
		log.dbg(this, "Available STUN servers: " + stunServers);
	}

	private InetSocketAddress getSTUNServerFromString(String str, ILog log) throws IllegalArgumentException, UnknownHostException {
		return InetAddressToolkit.getSocketAddressFromString(str, DEFAULT_STUN_SERVER_PORT);
	}

	@Override
	public InetAddress discover(ILog log) throws AddressDiscoveryException {
		return discoverShuffledSuccessiveRetry(log);
	}
	
	public InetAddress discoverShuffledSuccessiveRetry(ILog log) throws AddressDiscoveryException {
		return discoverShuffledSuccessiveRetry(stunServers, log);
	}
	
	public InetAddress discoverShuffledSuccessiveRetry(List<InetSocketAddress> stunServers, ILog log) throws AddressDiscoveryException {
		ArrayList<InetSocketAddress> stunServersInst = new ArrayList<InetSocketAddress>(stunServers.size());
		stunServersInst.addAll(stunServers);
		Collections.shuffle(stunServers, getRandom());
		return discoverSuccessiveRetry(stunServers, log);
	}
	
	public InetAddress discoverSuccessiveRetry(ILog log) throws AddressDiscoveryException {
		return discoverSuccessiveRetry(stunServers, log);
	}
	
	public InetAddress discoverSuccessiveRetry(List<InetSocketAddress> stunServers, ILog log) throws AddressDiscoveryException {
		checkSTUNServersEmpty(stunServers);
		int i = stunServers.size();
		for (InetSocketAddress srv : stunServers) {
			try {
				log.dbg(this, "Requesting public IP address on STUN server " + srv);
				return discover(srv, log);
			} catch (AddressDiscoveryException e) {
				log.warn(this, "Discovery on STUN server" + srv + " failed. " + i + " servers still remaining.", e);
			}
			i--;
		}
		throw new AddressDiscoveryException("Address discovery failed at all servers that were specified.");
		
	}
	
	public InetAddress discoverAtRandomServer(ILog log) throws AddressDiscoveryException {
		checkSTUNServersEmpty(stunServers);
		return discover(CollectionUtils.getRandomElementFrom(stunServers, new Random()), log);
	}
	
	public List<InetAddress> testDiscoveryOnAllServers(List<InetSocketAddress> stunServers, ILog log) throws AddressDiscoveryException {
		checkSTUNServersEmpty(stunServers);
		int i = stunServers.size();
		List<InetAddress> addressResults = new ArrayList<InetAddress>(stunServers.size());
		for (InetSocketAddress srv : stunServers) {
			try {
				log.dbg(this, "Testing STUN Req/Resp on server " + srv);
				addressResults.add(discover(srv, log));
			} catch (AddressDiscoveryException e) {
				log.warn(this, "Discovery on STUN server" + srv + " failed. ", e);
			}
			i--;
		}
		return addressResults;
	}

	public List<InetAddress> testDiscoveryOnAllServers(ILog log) throws AddressDiscoveryException {
		return testDiscoveryOnAllServers(stunServers, log);
	}
	
	public InetAddress discover(InetSocketAddress server, ILog log) throws AddressDiscoveryException {
		List<STUNAttribute> attrs = new ArrayList<STUNAttribute>(1);
		attrs.add(new STUNAttribute(STUNAttribute.ATTR_CHANGE_REQUEST, new byte[]{0,0,0,0}));
		//attrs.add(new STUNAttribute(STUNAttribute.ATTR_SOFTWARE, "Kangee  ".getBytes(Charset.forName("utf-8"))));
		STUNMessage msg = new STUNMessage(STUNMessage.MSG_BINDING_REQUEST, attrs);
		
		InetAddress addr = server.getAddress();
		if (addr == null) {
			try {
				addr = InetAddress.getByName(server.getHostName());
			} catch (UnknownHostException e) {
				throw new AddressDiscoveryException("Could not resolve address of host " + server.getHostName());
			}
		}
		STUNMessage response = sendSTUNMessageAndWait(msg, addr, server.getPort(), TIMEOUT, log);
		InetSocketAddress result = response.getMappedAddress();
		return result.getAddress();
	}
	
	private void checkSTUNServersEmpty(List<InetSocketAddress> stunServers)
			throws AddressDiscoveryException {
		if (stunServers.isEmpty()) throw new AddressDiscoveryException("No STUN servers are assigned.");
	}
	
	public Random getRandom() {
		return new Random();
	}
	
}
