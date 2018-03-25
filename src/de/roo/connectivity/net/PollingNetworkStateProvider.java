package de.roo.connectivity.net;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;

import de.roo.logging.ILog;

public class PollingNetworkStateProvider implements INetworkStateProvider {

	private Thread pollThread;
	
	NetworkInterface curIFOn;
	
	NetworkState lastSeenState = null;
	
	InetAddress lastSeenAddress = null;
	
	List<INetworkStateListener> listeners = new LinkedList<INetworkStateListener>();

	private ILog log;

	private List<InetAddress> lastAddresses;

	public PollingNetworkStateProvider(ILog log, final long pollInterval) {
		
		this.log = log;
		
		lastSeenState = refreshConnectionStatus(true);
		
		pollThread = new Thread() {
			
			public void run() {
				
				while(true) {
					
					try {
						Thread.sleep(pollInterval);
					} catch (InterruptedException e) {
						//Nothing to do, thread interrupted
					}
					
					pollOnce();
					
				}
				
			}
			
		};
		
		pollThread.start();
		
	}
	
	public void stop() {
		pollThread.interrupt();
	}
	
	protected void pollOnce() {
		
		NetworkState s = refreshConnectionStatus(false);
		
		if (s == NetworkState.On || s == NetworkState.Off) lastSeenState = s;
		
		if (s != NetworkState.Unchanged) {
			for (INetworkStateListener l : listeners) l.connectivityChanged(s);
		}
		
	}
	
	protected NetworkState refreshConnectionStatus(boolean initial) {
		
		try {
		
			if (curIFOn == null) {
					
				Enumeration<NetworkInterface> intfs = NetworkInterface.getNetworkInterfaces();
					
				for (Enumeration<NetworkInterface> e = intfs; e.hasMoreElements();) {
					NetworkInterface intf = e.nextElement();
					if (isUsefulDevice(intf) && hasConnectivity(intf)) {
						curIFOn = intf;
						
						rememberAddressesOf(intf);
						
						log.dbg(this, "Interface " + intf + " appears to be on now. Setting connectivity status to ON.");
						/*
						 * offline, now online.
						 */
						return NetworkState.On;
					}
				}
					
				/*
				 * still offline
				 */
				return initial?NetworkState.Off:NetworkState.Unchanged;

			}
			
			if (!curIFOn.isUp()) {
				
				Enumeration<NetworkInterface> intfs = NetworkInterface.getNetworkInterfaces();
				
				for (Enumeration<NetworkInterface> e = intfs; e.hasMoreElements();) {
					NetworkInterface intf = e.nextElement();
					if (intf != curIFOn && isUsefulDevice(intf) && hasConnectivity(intf)) {
						curIFOn = intf;
						
						log.dbg(this, "Interface " + intf + " appears to be on now instead of " + curIFOn
								+ ". Setting connectivity status to ONBUTCHANGED.");
						
						rememberAddressesOf(intf);
						
						/*
						 * Online, but changed network interface.
						 */
						return NetworkState.OnButChanged;
					}
				}
				curIFOn = null;
				
				log.dbg(this, "Interface " + curIFOn + " is offline now. Settin connectivity status to OFF.");
				return NetworkState.Off;
			}
			
			/*
			 * Addresses have changed.
			 */
			if (changedIP(curIFOn)) {
				log.dbg(this, "Interface " + curIFOn + " appears to be on with different addresses. " +
						"Setting connectivity status to ONBUTCHANGED.");
				return NetworkState.OnButChanged;
			}
			
			/*
			 * still online with the same interface.
			 */
			return NetworkState.Unchanged;
			
		
		} catch (SocketException e) {
			throw new RuntimeException(e);
		}
			
	}
	
	private boolean changedIP(NetworkInterface intf) {
		//long time = System.currentTimeMillis();
		List<InetAddress> addrs = enumerationsToList(intf.getInetAddresses());
		//System.out.println("Time to gather: " + (System.currentTimeMillis() - time) + " msec");
		boolean changed = !addrs.equals(lastAddresses);
		//System.out.println("New  : " + addrs);
		//System.out.println("Last : " + lastAddresses);
		lastAddresses =	addrs;
		return changed;
	}
	
	private void rememberAddressesOf(NetworkInterface intf) {
		lastAddresses =	enumerationsToList(intf.getInetAddresses());
	}

	private boolean isUsefulDevice(NetworkInterface intf) throws SocketException {
		return !intf.isLoopback();
	}

	protected boolean hasConnectivity(NetworkInterface intf) throws SocketException {
		
		return intf.isUp();
		
	}

	@Override
	public void addListener(INetworkStateListener l) {
		listeners.add(l);
	}

	@Override
	public void removeListener(INetworkStateListener l) {
		listeners.remove(l);
	}

	@Override
	public NetworkState getCurrentNetworkState() {
		return lastSeenState;
	}
	
	public static <T> List<T> enumerationsToList(Enumeration<T> enumeration) {
		List<T> l = new ArrayList<T>();
		
		for (Enumeration<T> e = enumeration; e.hasMoreElements();) {
			T elem = e.nextElement();
			l.add(elem);
		}
		return l;
	}

	
	
}
