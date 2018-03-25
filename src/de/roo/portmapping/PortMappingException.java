package de.roo.portmapping;

/**
 * 
 * @author Leo Nobach
 *
 */
public class PortMappingException extends Exception {

	public PortMappingException(String msg) {
		super(msg);
	}
	
	public PortMappingException(Throwable cause) {
		super(cause);
	}
	
	public PortMappingException(String msg, Throwable cause) {
		super(msg, cause);
	}

	public PortMappingException() {
		super();
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = -3377525810751135476L;
	
	public static class PortMappingConflict extends PortMappingException {

		public PortMappingConflict() {
			super();
		}

		public PortMappingConflict(String msg, Throwable cause) {
			super(msg, cause);
		}

		public PortMappingConflict(String msg) {
			super(msg);
		}

		public PortMappingConflict(Throwable cause) {
			super(cause);
		}

		/**
		 * 
		 */
		private static final long serialVersionUID = -9188194018843365437L;
		
	}
	
	public static class PortMappingNotAllowed extends PortMappingException {

		/**
		 * 
		 */
		private static final long serialVersionUID = 6278414335648111497L;

		public PortMappingNotAllowed() {
			super();
		}

		public PortMappingNotAllowed(String msg, Throwable cause) {
			super(msg, cause);
		}

		public PortMappingNotAllowed(String msg) {
			super(msg);
		}

		public PortMappingNotAllowed(Throwable cause) {
			super(cause);
		}
		
		
		
	}

}
