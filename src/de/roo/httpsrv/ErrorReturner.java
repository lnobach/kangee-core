package de.roo.httpsrv;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import de.roo.srvApi.BadPOSTException;
import de.roo.srvApi.IPOSTFileTransferInfo;
import de.roo.srvApi.IRequest;
import de.roo.srvApi.IRequestHandler;
import de.roo.srvApi.IResponseFactory;
import de.roo.srvApi.IServerContext;
import de.roo.srvApi.ServerException;

/**
 * 
 * @author Leo Nobach
 *
 */
public class ErrorReturner implements IRequestHandler {

	private int statusCode;

	public ErrorReturner(int statusCode) {
		this.statusCode = statusCode;
	}
	
	@Override
	public void handleRequest(IRequest request, IResponseFactory f, IServerContext ctx)
			throws ServerException, IOException {
		
		f.createDefaultStatusResponse(statusCode);
		
	}

	@Override
	public File getFileNameForPOSTContent(IRequest req,
			Map<String, String> partHeaders, IPOSTFileTransferInfo info, IServerContext ctx)
			throws BadPOSTException {
		throw new BadPOSTException("Not supported");
	}

	
	
}
