package de.roo.httpsrv;

import de.roo.httpsrv.Server;
import de.roo.logging.ConsoleLog;
import de.roo.srvApi.ServerException;

/**
 * 
 * @author Leo Nobach (dev@getkangee.com)
 *
 */
public class HTTPServerTest {

	public static void main(String[] args) {
		try {
			new Server(23456, new ConsoleLog(), new TestHandler(), null);	
		} catch (ServerException e) {
			e.printStackTrace();
		}
	}
	
}
