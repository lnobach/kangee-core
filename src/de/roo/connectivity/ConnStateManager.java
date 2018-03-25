package de.roo.connectivity;

import java.util.LinkedList;
import java.util.List;

import de.roo.connectivity.net.INetworkStateListener;
import de.roo.connectivity.net.INetworkStateProvider;
import de.roo.connectivity.net.INetworkStateProvider.NetworkState;
import de.roo.connectivity.net.PollingNetworkStateProvider;
import de.roo.engine.RooEngine;
import de.roo.engine.RooEngine.SetupContextImpl;
import de.roo.engine.setup.ISetupFollower;
import de.roo.engine.setup.SetupException;
import de.roo.logging.ILog;

public class ConnStateManager implements IConnStateProvider {

	private boolean isActive = false;
	private ILog log;
	INetworkStateProvider prov;
	
	ConnectivityState currentState = null;
	
	private RooEngine eng;
	private ConnectivityInfo info;	
	
	public ConnStateManager(RooEngine eng, ILog log) {
		this.eng = eng;
		this.log = log;
	}
	
	public void activate(boolean setupSuccess, ConnectivityInfo info) {
		this.isActive  = true;
		this.info = info;
		
		prov = new PollingNetworkStateProvider(log, 2000);
		
		assert !setupSuccess || prov.getCurrentNetworkState() == NetworkState.On;
		
		currentState = setupSuccess?ConnectivityState.Available:ConnectivityState.ProbablyAvailable;
		
		prov.addListener(new INetworkStateListener() {
			
			@Override
			public void connectivityChanged(NetworkState currentState) {
				
				if (currentState == NetworkState.On || currentState == NetworkState.OnButChanged) {
					setCurrentState(ConnectivityState.SettingUp);
					setup();
				} else if (currentState == NetworkState.Off) {
					setCurrentState(ConnectivityState.Unavailable);
				}
				
			}
			
		});
	}
	
	@Override
	public void forceReset() {
		if (currentState == ConnectivityState.SettingUp) 
			throw new IllegalStateException("Can not reset connectivity while already setting up.");
		setCurrentState(ConnectivityState.SettingUp);
		setup();
	}
	
	protected void setCurrentState(ConnectivityState s) {
		this.currentState = s;
		for (IConnStateListener l : listeners) l.connectivityStateChanged(this, s);
	}

	protected void setup() {
		
		int recheckDelay = eng.getConfiguration().getValueInt("IfUpRecheckDelay", 3000);
		
		if (recheckDelay > 0) {
		
			log.dbg(this, "Waiting " + recheckDelay + " milliseconds for the operating system to " +
					"set up its network connection.");
			
			try {
				Thread.sleep(recheckDelay);
			} catch (InterruptedException e) {
				log.warn(this, "The recheck delay was interrupted.", e);
			}
		
		}
			
		//First, do unsetup.
		eng.unsetup();
		
		SetupContextImpl ctx = eng.new SetupContextImpl();
		
		boolean success = eng.setup(ctx, new ISetupFollower() {
			
			@Override
			public void setupStarted() {
				//Nothing to do
			}
			
			@Override
			public void setupFinished() {
				//Nothing to do
			}
			
			@Override
			public void setupFailed(SetupException e) {
				changeConnectivityJob(e.getMessage());
			}
			
			@Override
			public void setCurrentJob(String status) {
				changeConnectivityJob(status);
			}

		});
		
		info = ctx.getConnectivityInfo();
		
		setCurrentState(success&&!ctx.wasOverridden()?ConnectivityState.Available:ConnectivityState.ProbablyAvailable);
	}

	public boolean isActive() {
		return isActive;
	}
	
	List<IConnStateListener> listeners = new LinkedList<IConnStateListener>();

	@Override
	public void addListener(IConnStateListener l) {
		listeners.add(l);
	}

	@Override
	public void removeListener(IConnStateListener l) {
		listeners.remove(l);
	}

	@Override
	public ConnectivityState getCurrentConnectivityState() {
		return currentState;
	}

	@Override
	public ConnectivityInfo getCurrentConnectivityInfo() {
		return info;
	}

	void changeConnectivityJob(String connectivityJobDesc) {
		for (IConnStateListener l : listeners) l.connectivityJobChanged(this, connectivityJobDesc);
	}

}
