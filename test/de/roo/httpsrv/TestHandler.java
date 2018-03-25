package de.roo.httpsrv;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;

import de.roo.srvApi.BadPOSTException;
import de.roo.srvApi.HTMLDocumentResponse;
import de.roo.srvApi.IPOSTFileTransferInfo;
import de.roo.srvApi.IRequest;
import de.roo.srvApi.IRequestHandler;
import de.roo.srvApi.IResponseFactory;
import de.roo.srvApi.IServerContext;
import de.roo.srvApi.ServerException;

/**
 * 
 * @author Leo Nobach (dev@getkangee.com)
 *
 */
public class TestHandler implements IRequestHandler {

	@Override
	public void handleRequest(IRequest request, IResponseFactory f, IServerContext ctx) throws ServerException, IOException {
				
		if (request.getRequestedPath().equals("/")) {
			
			StringBuilder b = new StringBuilder();
			
			b.append("<h1>\"Roo\" Personal On-Demand Fileserver</h1>");
			b.append("<ul>");
			b.append("<li><a href=\"/headers.html\">Headers</a>");
			b.append("</ul>");
			
			new HTMLDocumentResponse(200, f, b.toString().getBytes());
			
		} else if (request.getRequestedPath().equals("/headers.html")) {

			
			StringBuilder b = new StringBuilder();
			b.append("<h1>Headers:</h1>");

			b.append("<table>");
			b.append("<tr><td><b>Key</b></td><td><b>Value</b></td><tr>");
			for (Entry<String, String> e : request.getHeaders().entrySet())
				b.append("<tr><td>" + e.getKey() + "</td><td>" + e.getValue() + "</td><tr>");
			b.append("</table>");
			
			
			new HTMLDocumentResponse(200, f, b.toString().getBytes());
			
		} else {
			f.createDefaultStatusResponse(404);
		}
		
	}

	@Override
	public File getFileNameForPOSTContent(IRequest req,
			Map<String, String> partHeaders, IPOSTFileTransferInfo info, IServerContext ctx)
			throws BadPOSTException {
		throw new BadPOSTException("Unsupported");
	}

}
