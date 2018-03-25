package de.roo.httpsrv;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.List;

import de.roo.configuration.IConf;
import de.roo.http.ReqMethod;
import de.roo.http.Version;
import de.roo.httpsrv.security.AntiBruteforce;
import de.roo.logging.ILog;
import de.roo.srvApi.BadPOSTException;
import de.roo.srvApi.CancelledException;
import de.roo.srvApi.IPOSTedFile;
import de.roo.srvApi.IRequestHandler;
import de.roo.srvApi.IServerContext;
import de.roo.srvApi.ServerException;
import de.roo.srvApi.UnnecessaryPostException;
import de.roo.srvApi.security.IAntiBruteforce;
import de.roo.util.stream.LimitedEOFStream;
import de.roo.util.stream.StreamToolkit;

/**
 * 
 * @author Leo Nobach
 *
 */
public class Server {

	public static boolean USE_GZIP = false;
	private ILog log;
	private int port;
	private IRequestHandler hdlr;
	private ForkThread forkThread;
	private IConf conf;
	
	static final Version HTTP_1_0 = new Version("HTTP/1.0");
	static final Version HTTP_1_1 = new Version("HTTP/1.1");
	
	static final String CRLF = "\r\n";
	static final byte[] byteLineSep = CRLF.getBytes();
	public static final long RESTART_PAUSE = 200;
	AntiBruteforce<InetAddress> antiBF;
	
	boolean up = false;

	public Server(int port, ILog log, IRequestHandler hdlr, IConf conf) throws ServerException {
		this.log = log;
		this.port = port;
		this.hdlr = hdlr;
		this.conf = conf;
		init();
	}
	
	private void init() throws ServerException {
		ServerSocket sock;
		
		log.dbg(this, "Starting server " + this.getClass());
		
		antiBF = new AntiBruteforce<InetAddress>(conf, log);
		
		try {

			sock = new ServerSocket(port);
			forkThread = new ForkThread(sock);
			forkThread.start();
			up = true;
			
		} catch (IOException e) {
			throw new ServerException("Could not bind the server to port " + port, e);
		}
	}
	
	public void handleRequest(Socket connSocket) throws ServerException {
		try {
			
			RequesterInfo requesterInfo = new RequesterInfo(connSocket);
			
			log.dbg(this, "Connection to " + requesterInfo + " started." + ": " + connSocket);
			
			InputStream s = connSocket.getInputStream();
			BufferedInputStream buf = new BufferedInputStream(s);
			OutputStream os = connSocket.getOutputStream();
			
			boolean keepalive = false;
			
			do {
				ResponseFactory rf = null;
				
				try {
					String line;
				
					String reqLine = readLine(buf);
					if (reqLine == null) {
						throw new CancelledException("Client has finished connection before sending a request line.");
					}
					if (isHTTPDebugMode()) printHTTPDebugLine(reqLine, false);
					DefaultRequest req = new DefaultRequest(reqLine, requesterInfo);
					while (true) {
						line = readLine(buf);
						if (line == null) throw new ServerException(connSocket  + "Premature end of request");
						if (isHTTPDebugMode()) printHTTPDebugLine(line, false);
						if ("".equals(line)) break;
						req.parseAndAdd(line);
					}
					boolean keepAliveReq = shallKeepAlive(req);
					
					try {
					
						try {
						
							if (req.getReqMethod() == ReqMethod.POST) {
								long cLen = Long.parseLong(req.getHeader("Content-Length"));
								log.dbg(this, "POST length is " + cLen);
								POSTHandler pHndlr = new POSTHandler(log, hdlr, ctx);
								LimitedEOFStream postStream = new LimitedEOFStream(buf, cLen);
								if (isHTTPDebugMode()) printHTTPDebugLine("[POST Content]", false);
								List<IPOSTedFile> postResult = pHndlr.handlePOST(postStream, req);
								req.setPOSTedFiles(postResult);
								long skippedBytes = postStream.skipToEOF();
								log.dbg(this, "POST: Skipped " + skippedBytes + " bytes");
							}
							
						} catch (UnnecessaryPostException e) {
							log.warn(this, "Unnecessary POST", e);
							keepAliveReq = false;
						}
						rf = new ResponseFactory(this, os, req, HTTP_1_1, keepAliveReq);
						hdlr.handleRequest(req, rf, ctx);
						if (isHTTPDebugMode()) printHTTPDebugLine("[Response Content]", true);
						
						keepalive = rf.shallKeepAlive();
						rf.finish();
						//log.dbg(this, "Req/Resp as expected.");
						
					} catch (BadPOSTException ex) {
						log.warn(this, "Bad Request."  + ": " + connSocket, ex);
						new ErrorReturner(400).handleRequest(req, new ResponseFactory(this, os, req, new Version(), false), ctx);
						keepalive = false;
					} catch (CancelledException ex) {
						log.dbg(this, "Transmission cancelled by user."  + ": " + connSocket);
						keepalive = false;
					} catch (ServerException ex) {
						log.warn(this, "Internal Server Error"  + ": " + connSocket, ex);
						if (rf == null)
							new ErrorReturner(500).handleRequest(null, new ResponseFactory(this, os, null, new Version(), false), ctx);
						else if (!rf.isResponseCreated())
							new ErrorReturner(500).handleRequest(null, rf, ctx);
					}
					
				} catch (CancelledException ex) {
					log.dbg(this, "Transmission cancelled by user."  + ": " + connSocket);
					keepalive = false;
				} catch (ServerException ex) {
					log.warn(this, "Internal Server Error"  + ": " + connSocket, ex);
					if (rf == null)
						new ErrorReturner(500).handleRequest(null, new ResponseFactory(this, os, null, new Version(), false), ctx);
					else if (!rf.isResponseCreated())
						new ErrorReturner(500).handleRequest(null, rf, ctx);
				}
				
				log.dbg(this, "Keep-Alive established: " + keepalive + ": " + connSocket); 
				
				os.flush();
				
				if (antiBF.isBlocked(requesterInfo.getRequesterIP())) keepalive = false;
					//Else, an attacker who has opened many connections can go on attacking via them.
				
			} while (keepalive);
			
			os.flush();
			os.close();
			connSocket.close();
			
		} catch (SocketException e) {
			throw new ServerException("Client has closed unexpectedly.", e);
		} catch (IOException e) {
			throw new ServerException("I/O problems while communicating with the client.", e);
		}
	}
	
	private String readLine(InputStream is) throws IOException {
		return StreamToolkit.readLine(is);
	}

	private boolean shallKeepAlive(DefaultRequest req) {
		
		String connHdr = req.getHeader("Connection");
		if (connHdr == null) return false;
		String[] tokens = connHdr.split(",");
		for (String token : tokens) {
			return "keep-alive".equalsIgnoreCase(token.trim());
		}
		return false;
	}

	class ForkThread extends Thread {
		
		private ServerSocket sock;
		boolean terminated = false;

		public ForkThread(ServerSocket sock) {
			this.sock = sock;
		}
		
		public void run() {
			while(!terminated) {
				try {
					Socket socket = sock.accept();
					if (!antiBF.isBlocked(socket.getInetAddress())) {
						new WorkerThread(socket).start();
					} else {
						socket.close();
					}
				} catch (SocketException e) {
					if (!terminated) log.dbg(this, "Socket of server was closed unexpectedly.");
				} catch (IOException e) {
					log.error(this, "Problems while dispatching request", e);
				}
			}
		}
		
		public void terminate() {
			terminated = true;
			this.interrupt();
			try {
				sock.close();
			} catch (IOException e) {
				log.error(this, "Socket could not be closed. Server may not have been terminated.", e);
			}
		}
		
	}
	
	public void stopServer() {
		log.dbg(this, "Stopping server " + this);
		forkThread.terminate();
		up = false;
	}
	
	public boolean isUp() {
		return up;
	}
	
	class WorkerThread extends Thread {

		private Socket socket;

		public WorkerThread(Socket socket) {
			this.socket = socket;
		}
		
		public void run() {
			try {
				handleRequest(socket);
			} catch (ServerException e) {
					log.warn(this, "Problems while handling request", e);
			}
		}
		
	}
	
	public boolean isHTTPDebugMode() {
		return conf.getValueBoolean("DBG_HTTP_Protocol", false);
	}
	
	public void printHTTPDebugLine(String line, boolean send) {
		log.dbg(this, "HTTP: " + (send?"<  ":" > ") + line);
	}
	
	public ILog getLog() {
		return log;
	}
	
	final IServerContext ctx = new IServerContext() {

		@Override
		public IAntiBruteforce<InetAddress> getAntiBF() {
			return antiBF;
		}
		
	};

	public int getPort() {
		return port;
	}
	
}
