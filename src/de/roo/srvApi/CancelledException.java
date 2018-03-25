package de.roo.srvApi;

/**
 * 
 * @author Leo Nobach
 *
 */
public class CancelledException extends ServerException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6477378288404278361L;

	public CancelledException(String errorDetail) {
		super(errorDetail);
		
	}

}
