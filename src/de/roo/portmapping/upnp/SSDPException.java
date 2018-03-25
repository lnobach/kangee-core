package de.roo.portmapping.upnp;

/**
 * 
 * @author Leo Nobach
 *
 */
public class SSDPException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 297880591727903679L;

	public SSDPException(String msg) {
		super(msg);
	}
	
	public SSDPException(Throwable cause) {
		super(cause);
	}
	
	public SSDPException(String msg, Throwable cause) {
		super(msg, cause);
	}
	
}
