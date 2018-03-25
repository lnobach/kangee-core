package de.roo.portmapping;

import java.io.IOException;

import de.roo.BuildConstants;
import de.roo.configuration.DefaultConfiguration;
import de.roo.configuration.IConf;
import de.roo.connectivity.RemoteDiscovery;
import de.roo.connectivity.RemoteDiscovery.CheckConnectivityState;
import de.roo.connectivity.RemoteDiscovery.DiscoveryInfo;
import de.roo.httpcli.HttpClientException;
import de.roo.httpsrv.Server;
import de.roo.logging.ConsoleLog;
import de.roo.logging.ILog;
import de.roo.portmapping.Holepunch.HolepunchInfo;
import de.roo.srv.RooPingHandler;

public class HolepunchTest {

	public static void main(String[] args) {
		
		IConf conf = new DefaultConfiguration();
		
		String discoveryURL = BuildConstants.Default_Discovery_URLs[0];
		
		String rooID = "12345";
		
		ILog log = new ConsoleLog();
		
		long startTime = System.currentTimeMillis();
		
		log(startTime, "=====> Starting hole punching...");
		
		HolepunchInfo info;
		try {
			
			
			
			info = Holepunch.getPortFromHolepunch(log);
			
			log(startTime, "<===== Got back holepunch info: " + info + " Now doing connection test...");
			
			Server srv = new Server(info.getInternalPort(), log, new RooPingHandler(log, rooID), conf);

			log(startTime, "=====> Sending request to check connectivity.");
			
			DiscoveryInfo result = RemoteDiscovery.discover(BuildConstants.Default_Discovery_URLs[0], info.getExternalPort(), true, rooID);
			
			log(startTime, "<===== Response received.");
			
			srv.stopServer();
			
			System.out.println(result);
			
			if (result.getCheckState() == CheckConnectivityState.OK) {
				System.out.println("HOLE PUNCHING WAS SUCCESSFUL!");
			} else {
				System.out.println("HOLE PUNCHING WAS NOT SUCCESSFUL!");
			}
			
			System.exit(0);
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		
	}
	
	public static void log(long startTime, String logString) {
		System.out.println("Time: " + (System.currentTimeMillis() - startTime) + " msec: " + logString);
	}
	
}
