package de.roo.srvApi;

/**
 * 
 * @author Leo Nobach
 *
 */
public class ServerException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5985784216590116833L;
	public String getErrorDetail() {
		return errorDetail;
	}

	public Exception getCause() {
		return cause;
	}

	private String errorDetail;
	private Exception cause;
	
	public ServerException(String errorDetail, Exception cause) {
		this.errorDetail = errorDetail;
		this.cause = cause;
	}
	
	public ServerException(String errorDetail) {
		this.errorDetail = errorDetail;
		this.cause = null;
	}
	
	public String getMessage() {
		return "Server Problem: " + errorDetail;
	}

}
