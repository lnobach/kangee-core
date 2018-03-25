package de.roo.connectivity.net;

import de.roo.connectivity.net.INetworkStateProvider.NetworkState;

public interface INetworkStateListener {

	/**
	 * The method guarantees that there are no further simultaneous calls of 
	 * connectivityChanged(..) until the method has returned.
	 * @param currentState
	 */
	public void connectivityChanged(NetworkState currentState);
	
}
