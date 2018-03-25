package de.roo.connectivity;

import de.roo.connectivity.IConnStateProvider.ConnectivityState;

public interface IConnStateListener {
	
	public void connectivityStateChanged(IConnStateProvider src, ConnectivityState state);
	
	/**
	 * This can return NULL, then fall back to a default message or a description of ConnectivityState.
	 * @param prov
	 * @param connectivityJobDesc
	 */
	public void connectivityJobChanged(IConnStateProvider prov, String connectivityJobDesc);
	
}
