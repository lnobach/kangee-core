package de.roo.portmapping;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

import de.roo.logging.ILog;
import de.roo.portmapping.IPortMappingDevice.Protocol;
import de.roo.portmapping.PortMappingException.PortMappingNotAllowed;
import de.roo.util.InetAddressToolkit;

/**
 * 
 * @author Leo Nobach
 *
 */
public class ConsolePortMapper {

	
	
	static final int LOG_TO_TEXTFILE = 0;	//0 = debug, 1 = warning, 2 = error
	static final int LOG_TO_SYSOUT = 1;	//0 = debug, 1 = warning, 2 = error
	
	static final String LOG_FILE = "portmapping.log";
	static String logFile = null;
	
	private PrintStream out;
	private InputStream in;
	
	BufferedWriter fout = null;
	
	public ConsolePortMapper(PrintStream out, InputStream in) {
		this.out = out;
		this.in = in;
	}
	
	public int handle(String[] args) {
		out.println("Welcome to the Kangee Console Port Mapper");
		prepareLogFOut(null);
		int returnCode = discoverDevicesAndPrompt();
		closeLogFOut();
		return returnCode;
	}

	private int discoverDevicesAndPrompt(){
		out.println("Searching for port mapping devices...");
		try {
			List<IPortMappingDevice> devs = PortMapping.discoverAllDevices(log);
			
			out.println("Finished port mapping discovery.");
			if (devs.isEmpty()) {
				out.println("No port mapping devices were discovered.");
				return 3;
			} else {
				out.println("The following devices were discovered:");
				int i = 0;
				for (IPortMappingDevice dev : devs) {
					out.println(i + ": " + dev.getName() + " (type=" + dev.getTypeName() + ", id=" + dev.getID() + ")");
					i++;
				}
				IPortMappingDevice dev;
				while (true) {
					try {
						out.println("Note: The number in brackets is always the default value that is applied when you just hit enter and leave the fields blank.");
						dev = devs.get(promptPositiveInt("Type the number of the port mapping device you want to configure", 0));
						break;
					} catch (IndexOutOfBoundsException e) {
						out.println("Number is out of bounds.");
					}
				}
				return handle(dev);
			}
			
		} catch (PortMappingException e) {
			out.println("Problems while discovering port mapping devices.");
			e.printStackTrace();
			return 2;
		}
	}

	private int handle(IPortMappingDevice dev) {
		out.println("You have the following action options on " + dev.getName() + ": ");
		out.println("0: Forward a port");
		out.println("1: Remove a port mapping.");
		out.println("2: Return a specific port mapping.");
		out.println("3: Get the external IP address of this device.");
		out.println("4: List all port mappings.");
		int response;
		while (true) {
			response = promptPositiveInt("Choose an action", 0);
			if (response >= 0 && response <= 4) break;
			out.println("Please choose an action from 0 to 4.");
		}
		if (response == 0) return forwardPort(dev);
		if (response == 1) return removePMapping(dev);
		if (response == 2) return getEntryFor(dev);
		if (response == 3) return getAddress(dev);
		return getPortMappingList(dev);
	}

	private int getEntryFor(IPortMappingDevice dev) {
		int wanPort = promptPort("Global (WAN) port to query? ", -1);
		Protocol prot = promptProtocol("Which protocol?", Protocol.TCP);
		
		try {
			IPortMappingEntry e = dev.getPortMapping(wanPort, prot, log);
			if (e != null) {
				out.println(printPortMappingCaption());
				out.println(printPortMappingEntry(e));
			} else {
				out.println("No entry was found.");
			}
			return 0;
		} catch (PortMappingException e) {
			out.print("An error occured when retrieving the entry.");
			e.printStackTrace();
			return 7;
		}
	}

	private int removePMapping(IPortMappingDevice dev) {
		int limit = promptPositiveInt("Limit of entries?", 20);
		try {
			List<IPortMappingEntry> list = dev.getPortMappingEntryList(limit, log);
			if (list.isEmpty()) {
				out.println("No portmapping is there to be removed.");
				return 0;
			}
			out.println(printPortMappingCaption());
			int	i = 0;
			for (IPortMappingEntry e : list) {
				out.println(i + ": " + printPortMappingEntry(e));
				i++;
			}
			
			int entryN = -1;
			IPortMappingEntry e;
			while(true) {
				try {
					entryN = promptPositiveInt("Which entry shall be removed?", 0);
					e = list.get(entryN);
					break;
				} catch (IndexOutOfBoundsException ex) {
					out.println("Your choice '" + entryN + "' is out of bounds.");
				}
			}
			e.removePortMapping(log);
			out.println("The port forward removal was successfully done.");
		} catch (PortMappingException e) {
			e.printStackTrace();
			return 6;
		}
		return 0;
	}
	
	String printPortMappingCaption() {
		return "lanP,\twanP,\tprot,\tintCli,\tdescr";
	}
	
	String printPortMappingEntry(IPortMappingEntry e) {
		return e.getLanPort() + ",\t" + e.getWanPort() + ",\t" + e.getProtocol() + ",\t" + e.getInternalClient() + ",\t" + e.getDescription();
	}

	private int getPortMappingList(IPortMappingDevice dev) {
		int limit = promptPositiveInt("Limit of entries?", 20);
		try {
			List<IPortMappingEntry> list = dev.getPortMappingEntryList(limit, log);
			out.println(printPortMappingCaption());
			for (IPortMappingEntry e : list) {
				out.println(printPortMappingEntry(e));
			}
		} catch (PortMappingException e) {
			e.printStackTrace();
			return 6;
		}
		return 0;
	}

	private int getAddress(IPortMappingDevice dev) {
		try {
			InetAddress addr = dev.getExternalIPAddress(log);
			out.println("The device's external address is " + addr);
			return 0;
		} catch (PortMappingException e) {
			out.println("An error occured when retrieving the address.");
			e.printStackTrace();
			return 5;
		}
	}

	private int forwardPort(IPortMappingDevice dev) {
		out.println("Will now configure " + dev.getID() + ".");
	
		int lanPort = promptPort("Local (LAN) port to map? ", -1);
		int wanPort = promptPort("Global (WAN) port to map? ", lanPort);
		Protocol prot = promptProtocol("Which protocol to map?", Protocol.TCP);
		InetAddress internalClient = promptInetAddress("Map these ports to which internal client?", InetAddressToolkit.getDefaultLanAddr(log));
		String description = prompt("Optional description?", "Done by Kangee");
		
		int retCode;
		try {
			dev.forwardPort(lanPort, wanPort, prot, internalClient, description, log);
			out.println("The port forwarding was successfully done.");
			retCode = 0;
		} catch (PortMappingNotAllowed e) {
			out.println("Could not map ports: Port mapping is not allowed on device " + dev);
			retCode = 8;
		} catch (PortMappingException e) {
			out.println("Error while doing the port mapping.");
			e.printStackTrace();
			retCode = 4;
		}
		if (logFile != null) out.println("Please consult the log file " + logFile + " for details."); 
		return retCode;
	}
	
	protected InetAddress promptInetAddress(String message, InetAddress defaultAddr) {
		
		String defaultAddrStr = defaultAddr.getHostAddress();
		
		String inetAddrStr = prompt(message, defaultAddrStr);
		while (true) {
			try {
				return InetAddress.getByName(inetAddrStr);
			} catch (UnknownHostException e) {
				out.println("Could not recognize your input '" + inetAddrStr + "' as an IP address or hostname.");
				e.printStackTrace();
			}
		}
	}
	
	protected Protocol promptProtocol(String message, Protocol defaultProt) {
		while (true) {
			String result = prompt(message + " 1:TCP or 2:UDP", defaultProt.toString());
			if ("0".equals(result)) return Protocol.TCP;
			if ("1".equals(result)) return Protocol.UDP;
			if ("tcp".equalsIgnoreCase(result)) return Protocol.TCP;
			if ("udp".equalsIgnoreCase(result)) return Protocol.UDP;
			out.println("Please enter either 0, 1, tcp or udp.");
		}
	}
	
	protected int promptPort(String message, int defaultPort) {
		while (true) {
			int result = promptPositiveInt(message, defaultPort);
			if (result > 0 && result < 65535) return result;
			out.println("Port is out of range: (< 0 or > 65535)");
		}
	}

	protected int promptPositiveInt(String message, int defaultValue) {
		
		String defaultStr = defaultValue >= 0 ? String.valueOf(defaultValue) : null;
		
		while (true) {
			try {
				return Integer.parseInt(prompt(message, defaultStr));
			} catch (NumberFormatException e) {
				out.println("This is not an integer number.");
			}
		}
	}
	
	/**
	 * If defaultValue is null, no default value will be accepted.
	 * @param message
	 * @param defaultValue
	 * @return
	 */
	protected String prompt(String message, String defaultValue) {
		
		out.print(message);
		if (defaultValue != null) out.print(" [" + defaultValue + "]");
		out.println();
		out.print(" >");
		StringBuffer buf = new StringBuffer();
		try {
			int inCh;
			while((inCh = in.read()) != '\n') {
				buf.append((char)inCh);
			}
		} catch (IOException e) {
			out.println("Input cancelled due to an IO exception.");
			e.printStackTrace();
			return "";
		}
		String result = buf.toString();
		if ("".equals(result.trim()) && defaultValue != null) return defaultValue;
		return result;
	}
	
	ILog log = new ILog(){

		@Override
		public void dbg(Object src, Object msg) {
			String str = "DBG:" + msg + "(" + src + ")";
			log(0, str);
		}

		@Override
		public void error(Object src, Object msg) {
			String str = "ERR:" + msg + "(" + src + ")";
			log(2, str);
		}

		@Override
		public void error(Object src, Object msg, Throwable ex) {
			String str = "ERR:" + "(" + src + ")";
			ex.printStackTrace();
			log(2, str);
		}

		@Override
		public void warn(Object src, Object msg) {
			String str = "WRN:" + "(" + src + ")";
			log(1, str);
		}

		@Override
		public void warn(Object src, Object msg, Throwable ex) {
			String str = "WRN:" + "(" + src + ")";
			ex.printStackTrace();
			log(1, str);
		}
		
		void log(int severity, String str) {
			if (severity >= LOG_TO_SYSOUT) out.println(str);
			if (fout != null && severity >= LOG_TO_TEXTFILE)
				try {
					fout.write(str + "\n");
				} catch (IOException e) {
					//Nothing to do
				}
		}
		
	};
	
	
	public void prepareLogFOut(String fileName) {
		if (LOG_TO_TEXTFILE <= 2) {
			if (fileName == null) logFile = LOG_FILE;
			else logFile = fileName;
			try {
				FileWriter fw = new FileWriter(logFile);
				fout = new BufferedWriter(fw);
			} catch (IOException e) {
				out.println("Could not open file " + fileName + ".");
				e.printStackTrace();
			}
		}
	}
	
	public void closeLogFOut() {
		if (fout != null)
			try {
				fout.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}
	
	
	public static void main(String[] args) {
		ConsolePortMapper mapper = new ConsolePortMapper(System.out, System.in);
		System.exit(mapper.handle(args));
	}
	
}
