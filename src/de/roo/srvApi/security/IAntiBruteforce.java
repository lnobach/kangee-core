package de.roo.srvApi.security;

/**
 * 
 * @author Leo Nobach
 *
 */
public interface IAntiBruteforce<TAddress> {

	public abstract boolean isBlocked(TAddress addr);

	/**
	 * Returns whether the contact is blocked after this attempt.
	 * @param addr
	 * @param hint4log
	 * @return
	 */
	public abstract boolean noteFailedAttemptFrom(TAddress addr, String hint4log);

}