package de.roo;

import de.roo.util.VersionInfo;

/**
 * Contains constants that are used throughout the application. They
 * are rather frequently changed.
 * 
 * @author Leo Nobach
 *
 */
public class BuildConstants {
	
	/**
	 * Major revision integer number of the application
	 */
	static final int MAJOR = 0;
	
	/**
	 * Minor revision integer number of the application
	 */
	static final int MINOR = 18;

	/**
	 * The full version as a String.
	 */
	public static final String PROD_VER = MAJOR + "." + MINOR;
	
	/**
	 * The object representation of the local product version.
	 * @return
	 */
	public static VersionInfo getLocalProdVersion() {
		return new VersionInfo(MAJOR, MINOR);
	}
	
	/**
	 * The short name of the application.
	 */
	public static final String PROD_TINY_NAME = "Kangee";
	
	/**
	 * The full name of the application
	 */
	public static final String PROD_FULL_NAME = PROD_TINY_NAME + " Personal On-Demand Fileserver";

	/**
	 * The application name with the version, as it is expected in the HTTP header field.
	 */
	public static final String PROD_TINY_NAME_VER = PROD_TINY_NAME + "/" + PROD_VER;
	
	/**
	 * The URL of the project homepage.
	 */
	public static final String PROD_URL = "http://getkangee.com";
	
	/**
	 * Default connection test servers which are presented to the user
	 */
	public static final String[] Default_Discovery_URLs=new String[] {
		"http://getkangee.com/api/conntest.php",
		"http://ipv6.getkangee.com/api/conntest.php",
		"http://ipv4.getkangee.com/api/conntest.php"
	};
	
	/**
	 * The default theme the Kangee HTTP server shall take.
	 */
	public static final String DEFAULT_PRESENTATION_THEME = "kangaroo";
	
	/**
	 * The default port Kangee suggests.
	 */
	public static final int DEFAULT_PORT = 8090;
	
	/**
	 * The URL to the XML document about the most recent version.
	 */
	public static final String UPDATE_CHECK_URL = "http://getkangee.com/api/update.xml";
	
	/**
	 * The URL that is suggested if the user is running an old version.
	 */
	public static final String UPDATE_DOWNLOAD_URL = "http://getkangee.com/getkangee";

	/**
	 * Timeout to connect to the distributor server in milliseconds.
	 */
	public static final int DISTR_SERVER_CONNECT_TIMEOUT = 5000;

	public static final int DISTR_SERVER_READ_TIMEOUT = 5000;
	
}
