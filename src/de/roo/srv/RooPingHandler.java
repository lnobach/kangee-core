package de.roo.srv;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Map;

import de.roo.http.ReqMethod;
import de.roo.logging.ILog;
import de.roo.srvApi.BadPOSTException;
import de.roo.srvApi.IPOSTFileTransferInfo;
import de.roo.srvApi.IRequest;
import de.roo.srvApi.IRequestHandler;
import de.roo.srvApi.IResponse;
import de.roo.srvApi.IResponseFactory;
import de.roo.srvApi.IServerContext;
import de.roo.srvApi.ResponseHeaders;
import de.roo.srvApi.ServerException;

/**
 * 
 * @author Leo Nobach
 *
 */
public class RooPingHandler implements IRequestHandler {

	//private ILog log;

	private String rooID;

	public RooPingHandler(ILog log, String rooID) {
		//this.log = log;
		this.rooID = rooID;
	}
	
	@Override
	public void handleRequest(IRequest request, IResponseFactory f, IServerContext ctx)
			throws ServerException, IOException {

		String reqPath = request.getRequestedPath();
		ReqMethod method = request.getReqMethod();
		
		if (method != ReqMethod.GET) {
			unsupported(f);
			return;
		}
		
		if (!reqPath.startsWith("/")) {
			notfound(f);
			return;
		}
		
		if ("/ping".equals(reqPath)) {
			answerPong(f);
			return;
		}
		
		notfound(f);
		
	}
	
	private void answerPong(IResponseFactory f) throws IOException, ServerException {
		ResponseHeaders hdrs = new ResponseHeaders();
		hdrs.addHeader("Content-Type", "text/plain");
		hdrs.setContentLength(9 + rooID.length());
		IResponse resp = f.createResponse(200, hdrs);
		OutputStream os = resp.getResponseStream();
		OutputStreamWriter wr = new OutputStreamWriter(os);
		BufferedWriter buf = new BufferedWriter(wr);
		buf.write("ROO-PONG_" + rooID);
		buf.flush();
		
	}

	private void notfound(IResponseFactory f) throws ServerException, IOException {
		f.createDefaultStatusResponse(404);
	}
	
	private void unsupported(IResponseFactory f) throws ServerException, IOException {
		f.createDefaultStatusResponse(405);
	}

	@Override
	public File getFileNameForPOSTContent(IRequest req,
			Map<String, String> partHeaders, IPOSTFileTransferInfo info, IServerContext ctx)
			throws BadPOSTException {
		throw new BadPOSTException("Unsupported");
	}
	
}





