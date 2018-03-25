/**
 * 
 */
package de.roo.httpsrv;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Map.Entry;
import java.util.zip.GZIPOutputStream;

import de.roo.http.HttpStatusCodes;
import de.roo.http.Version;
import de.roo.logging.ILog;
import de.roo.srvApi.HTMLDocumentResponse;
import de.roo.srvApi.IRequest;
import de.roo.srvApi.IResponse;
import de.roo.srvApi.IResponseFactory;
import de.roo.srvApi.ResponseHeaders;
import de.roo.srvApi.ServerException;
import de.roo.util.server.HeaderToolkit;

/**
 * 
 * @author Leo Nobach
 *
 */
class ResponseFactory implements IResponseFactory {

	private OutputStream responseStr;
	private Version ver;

	private ResponseHeaders requiredHdrs = new ResponseHeaders(false);

	boolean responseCreated = false;
	private IRequest request;
	private GZIPOutputStream gzip = null;
	private Server server;
	private boolean keepAliveReq; // Keep-Alive requested
	private boolean keepAlive; // Keep-Alive shall be made
	private long len = -1;
	private ILog log;

	Response respCreated;

	public ResponseFactory(Server server, OutputStream responseStr,
			IRequest request, Version ver, boolean keepAliveReq) {
		this.responseStr = responseStr;
		this.ver = ver;
		this.request = request;
		this.server = server;
		this.keepAliveReq = keepAliveReq;
		this.log = server.getLog();
	}

	void chechResponseCreated() throws ServerException {
		if (responseCreated)
			throw new ServerException("Cannot create multiple responses");
		responseCreated = true;
	}

	public void addRequiredHeader(String name, String value) {
		requiredHdrs.addHeader(name, value);
	}

	public boolean isResponseCreated() {
		return responseCreated;
	}

	@Override
	public IResponse createResponse(int httpStatus, ResponseHeaders hdrs)
			throws ServerException {
		chechResponseCreated();

		keepAlive = keepAliveReq && canKeepAlive(hdrs);
		addKeepAliveHeaders(keepAlive, request);

		hdrs.invalidate();

		boolean gZip = (request != null) && HeaderToolkit.allowsGZip(request)
				&& useGZip();
		if (gZip)
			this.addRequiredHeader("Content-Encoding", "gzip");

		hdrs.overwrite(requiredHdrs);

		OutputStreamWriter wr = new OutputStreamWriter(responseStr);
		BufferedWriter buf = new BufferedWriter(wr);

		String httpStatusStr = HttpStatusCodes.fromID(httpStatus);

		try {

			String respLine = ver.toString() + " " + httpStatus + " "
					+ httpStatusStr;
			if (server.isHTTPDebugMode())
				server.printHTTPDebugLine(respLine, true);
			buf.write(respLine + Server.CRLF);

			for (Entry<String, String> header : hdrs.getAsMap().entrySet()) {
				String headerStr = header.getKey() + ": " + header.getValue();
				if (server.isHTTPDebugMode())
					server.printHTTPDebugLine(headerStr, true);
				buf.write(headerStr + Server.CRLF);
			}
			buf.write(Server.CRLF); // Indicates end of response headers
			buf.flush();

			OutputStream streamToUse = gZip ? gzip = new GZIPOutputStream(
					responseStr) : responseStr;
			respCreated = new Response(streamToUse);
			return respCreated;

		} catch (IOException e) {
			throw new ServerException("Could not write headers to client", e);
		}

	}

	private boolean canKeepAlive(ResponseHeaders hdrs) {
		// Can only keep alive if Content-Length was set.
		String lenStr = hdrs.getHeader("Content-Length");
		if (lenStr == null) {
			log.dbg(this, "Content length has not been set for current request. Will not establish Keep-Alive");
			return false;
		}
		try {
			this.len = Long.parseLong(lenStr);
			return true;
		} catch (NumberFormatException e) {
			log.error(this, "Content length is non-numeric!");
			return false;
		}
	}

	private boolean useGZip() {
		return Server.USE_GZIP;
	}

	@Override
	public void createDefaultStatusResponse(int statusCode)
			throws ServerException, IOException {

		StringBuilder str = new StringBuilder();
		
		str.append("<html><head> \r\n");
		str.append("<title>" + statusCode + " "
				+ HttpStatusCodes.fromID(statusCode) + "</title> \r\n");
		str.append("</head> \r\n");
		str.append("<body> \r\n");
		str.append("<H1>" + statusCode + " "
				+ HttpStatusCodes.fromID(statusCode) + "</H1> \r\n");
		str.append("</body></html> \r\n");
		
		new HTMLDocumentResponse(statusCode, this, str.toString().getBytes());
		
	}

	private void addKeepAliveHeaders(boolean keepAlive, IRequest req) {
		if (req == null) return;
		if (req.getVersion().equals(Server.HTTP_1_1) && !keepAlive) this.addRequiredHeader("Connection", "close");
		if (keepAlive) this.addRequiredHeader("Connection", "keep-alive");
	}

	public void finish() throws IOException {
		if (gzip != null) {
			gzip.finish();
		}
		if (keepAlive) {
			long realLen = respCreated.getResponseLength();
			if (len != respCreated.getResponseLength())
				log.error(this, "Response length (" + len
						+ ") and value of Content-length (" + realLen
						+ ") are not equal!");
		}
	}

	public boolean shallKeepAlive() {
		return keepAlive;
	}

}