package de.roo.connectivity;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;

import de.roo.configuration.IConf;
import de.roo.logging.ILog;
import de.roo.model.RooUploadResource;
import de.roo.model.uiview.IRooResource;

/**
 * 
 * @author Leo Nobach
 *
 */
public class ConnectivityToolkit {

	public static URL generateDownloadURL(ConnectivityInfo info, IConf conf, IRooResource res, ILog log, boolean lan) {
		if (info == null) log.error(ConnectivityToolkit.class, "The connectivity info is undefined.");
		InetAddress ip2use = lan||info.getInetIP()==null?info.getLanIP():info.getInetIP();
		
		if (ip2use instanceof Inet6Address) {
			//The only way to remove the scope ID that is not needed and confuses some
			//browsers.
			Inet6Address addr6 = (Inet6Address)ip2use;
			try {
				ip2use = InetAddress.getByAddress(addr6.getAddress());
			} catch (UnknownHostException e) {
				throw new RuntimeException(e); //Should never happen.
			}
		}
		
		String host = conf.getValueBoolean("Use_Hostnames", false)?ip2use.getHostName():ip2use.getHostAddress();
		String path;
		
		boolean direct = conf.getValueBoolean("Use_Direct_Downloads", false);
		
		if (res instanceof RooUploadResource) {
			RooUploadResource upRes = (RooUploadResource)res;
			path = "/" + res.getIdentifier() + (direct?"/" + upRes.getHttpFileName():"/");
		}
		else path = "/" + res.getIdentifier() + (direct?"/upload" : "/");
		try {
			return new URL("http", host, info.getPort(), path);
		} catch (MalformedURLException e) {
			log.error(ConnectivityToolkit.class, "Got malformed URL with host="
					+ host
					+ ", ip=" + info.getPort() + ", path=" + path + ". Returning null.");
		}
		return null;
	}
	
}
