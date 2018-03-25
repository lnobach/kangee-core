package de.roo.engine.setup;

import java.net.InetAddress;

/**
 * 
 * @author Leo Nobach
 *
 */
public class SetupException extends Exception {


	/**
	 * 
	 */
	private static final long serialVersionUID = 219668241292524436L;

	public SetupException() {
		super();
		
	}

	public SetupException(String message, Throwable cause) {
		super(message, cause);
		
	}

	public SetupException(String message) {
		super(message);
		
	}

	public SetupException(Throwable cause) {
		super(cause);
		
	}
	
	public static class NotConfigured extends SetupException {

		public NotConfigured() {
			super();
		}

		public NotConfigured(String message, Throwable cause) {
			super(message, cause);
		}

		public NotConfigured(String message) {
			super(message);
		}

		public NotConfigured(Throwable cause) {
			super(cause);
		}

		/**
		 * 
		 */
		private static final long serialVersionUID = 2231999718866088803L;
		
	}
	
	public static class BadConnectionTest extends SetupException {

		/**
		 * 
		 */
		private static final long serialVersionUID = 6180022099886474737L;
		public int getPort() {
			return port;
		}

		public InetAddress getLanAddr() {
			return lanAddr;
		}

		public InetAddress getWanAddress() {
			return wanAddress;
		}

		public String getPresentationURLSeen() {
			return presentationURLSeen;
		}

		private int port;
		private InetAddress lanAddr;
		private InetAddress wanAddress;
		private String presentationURLSeen;

		public BadConnectionTest(String message, int port, InetAddress lanAddr, InetAddress wanAddress, String presentationURLSeen, Exception e) {
			super(message, e);
			this.port = port;
			this.lanAddr = lanAddr;
			this.wanAddress = wanAddress;
			this.presentationURLSeen = presentationURLSeen;
		}

		public BadConnectionTest(String message, int port,
				InetAddress lanAddr, InetAddress wanAddress,
				String presentationURLSeen) {
			super(message);
			this.port = port;
			this.lanAddr = lanAddr;
			this.wanAddress = wanAddress;
			this.presentationURLSeen = presentationURLSeen;
		}
		
	}
	
}
