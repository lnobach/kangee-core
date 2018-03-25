package de.roo.engine.setup;

import de.roo.configuration.IWritableConf;
import de.roo.connectivity.ConnectivityInfo;
import de.roo.logging.ILog;

/**
 * 
 * @author Leo Nobach
 *
 */
public interface ISetupContext {
	
	public void setConnectivityInfo(ConnectivityInfo info);
	
	public IWritableConf getConf();
	
	public String getRooID();

	public ILog getLog();

	void addUnsetup(IUnSetup hdl);

	ConnectivityInfo getConnectivityInfo();

	int isServerRunning();

	/**
	 * The setup has ended, but connectivity establishment was overridden.
	 * Going on with a probably unavailable connectivity.
	 */
	public void setUnsuccessfulAndOverridden();
	
}
