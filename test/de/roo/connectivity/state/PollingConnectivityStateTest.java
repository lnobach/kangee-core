package de.roo.connectivity.state;

import de.roo.connectivity.net.INetworkStateListener;
import de.roo.connectivity.net.INetworkStateProvider;
import de.roo.connectivity.net.PollingNetworkStateProvider;
import de.roo.connectivity.net.INetworkStateProvider.NetworkState;
import de.roo.logging.ConsoleLog;

public class PollingConnectivityStateTest {

	public static void main(String[] args) {
		
		INetworkStateProvider prov = new PollingNetworkStateProvider(new ConsoleLog(), 2000);
		
		System.out.println("Connectivity state is currently " + prov.getCurrentNetworkState());
		
		prov.addListener(new INetworkStateListener() {
			
			@Override
			public void connectivityChanged(NetworkState currentState) {
				System.out.println("Listener: Connectivity changed to: " + currentState);
			}
		});
		
	}
	
}
