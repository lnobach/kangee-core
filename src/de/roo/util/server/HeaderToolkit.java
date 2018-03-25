package de.roo.util.server;

import de.roo.srvApi.IRequest;

/**
 * 
 * @author Leo Nobach
 *
 */
public class HeaderToolkit {

	public static boolean allowsGZip(IRequest req) {
		String header = req.getHeader("Accept-Encoding");
		if (header == null) return false;
		String[] tokens = header.split(",");
		for (String token : tokens) {
			if ("gzip".equals(token)) return true;
		}
		return false;
	}
	
}
