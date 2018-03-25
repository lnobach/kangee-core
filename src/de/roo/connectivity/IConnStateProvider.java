package de.roo.connectivity;

public interface IConnStateProvider {

	public enum ConnectivityState {
		
		Unavailable,
		UnavailableError,
		ProbablyAvailable,
		SettingUp,
		Available;
		
	}
	
	public void addListener(IConnStateListener l);
	
	public void removeListener(IConnStateListener l);
	
	public ConnectivityState getCurrentConnectivityState();
	
	public ConnectivityInfo getCurrentConnectivityInfo();
	
	public void forceReset();
	
}
