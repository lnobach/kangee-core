package de.roo.httpsrv.security;

import java.util.HashMap;
import java.util.Map;

import de.roo.configuration.IConf;
import de.roo.logging.ILog;
import de.roo.srvApi.security.IAntiBruteforce;

/**
 * 
 * @author Leo Nobach
 *
 */
public class AntiBruteforce<TAddress> implements IAntiBruteforce<TAddress> {

	final int maxAttempts;
	
	Map<TAddress, Integer> currentFailedAttempts;

	private ILog log;
	
	public AntiBruteforce(IConf conf, ILog log) {
		this.log = log;
		maxAttempts = conf.getValueInt("Sec_MaxFailedAttempts", 20);
		currentFailedAttempts = new HashMap<TAddress, Integer>();
	}
	
	@Override
	public boolean isBlocked(TAddress addr) {
		Integer attempts = currentFailedAttempts.get(addr);
		if (attempts == null || attempts < maxAttempts) return false;
		else {
			log.warn(this, "Nasty blocked address " + addr + " tries to connect. Will refuse connection.");
			return true;
		}
	}
	
	@Override
	public boolean noteFailedAttemptFrom(TAddress addr, String hint4log) {
		Integer attempts = currentFailedAttempts.get(addr);
		if (attempts == null) {
			log.dbg(this, "Failed resource access attempt from "
					+ addr + ". Hint: " + hint4log + " This is the first fail of this address.");
			currentFailedAttempts.put(addr, 1);
		}
		else {
			log.dbg(this, "Failed resource access attempt from "
					+ addr + ". Hint: " + hint4log + " This address failed " + attempts + " times before.");
			currentFailedAttempts.put(addr, attempts +1);
			if (attempts > maxAttempts) return true;
		}
		return false;
	}
	
}
