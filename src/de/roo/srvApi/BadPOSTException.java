package de.roo.srvApi;

/**
 * 
 * @author Leo Nobach
 *
 */
public class BadPOSTException extends ServerException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4899622195331773560L;

	public BadPOSTException(String errorDetail) {
		super(errorDetail);
	}
	
	public BadPOSTException(String errorDetail, Exception cause) {
		super(errorDetail, cause);
	}

}
