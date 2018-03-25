package de.roo.srvApi;

import java.net.InetAddress;

import de.roo.srvApi.security.IAntiBruteforce;

/**
 * 
 * @author Leo Nobach
 *
 */
public interface IServerContext {

	public IAntiBruteforce<InetAddress> getAntiBF();
	
}
