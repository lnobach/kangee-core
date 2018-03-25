package de.roo.connectivity.net;

public interface INetworkStateProvider {

	public enum NetworkState {
		
		/**
		 * The client has connected after being disconnected.
		 */
		On, 
		
		/**
		 * The client has disconnected after being connected.
		 */
		Off,
		
		/**
		 * The client was online and immediately got online with a different connectivity.
		 */
		OnButChanged,
		
		/**
		 * The internet connection is unchanged.
		 */
		Unchanged;
		
	}
	
	public NetworkState getCurrentNetworkState();
	
	public void addListener(INetworkStateListener l);
	
	public void removeListener(INetworkStateListener l);
	
}
