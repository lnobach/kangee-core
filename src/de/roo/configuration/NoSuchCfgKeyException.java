package de.roo.configuration;

/**
 * 
 * @author Leo Nobach
 *
 */
public class NoSuchCfgKeyException extends RuntimeException {

	
	/**
	 * 
	 */
	private static final long serialVersionUID = -4414035773995956611L;
	
	private String cfgKeyUsed;

	public NoSuchCfgKeyException(String cfgKeyUsed) {
		super("No such config key: " + cfgKeyUsed);
		this.cfgKeyUsed = cfgKeyUsed;
		
	}
	
	public String getCfgKeyUsed() {
		return cfgKeyUsed;
	}
	
}
