package de.roo.srvApi;

import java.io.File;
import java.io.IOException;
import java.util.Map;

/**
 * 
 * @author Leo Nobach
 *
 */
public interface IRequestHandler {

	public void handleRequest(IRequest request, IResponseFactory f, IServerContext ctx) throws ServerException, IOException;

	public File getFileNameForPOSTContent(IRequest req, Map<String, String> partHeaders, IPOSTFileTransferInfo info, IServerContext ctx) throws BadPOSTException;
	
}
