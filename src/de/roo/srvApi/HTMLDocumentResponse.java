package de.roo.srvApi;

import java.io.BufferedWriter;
import java.io.IOException;

/**
 * 
 * @author Leo Nobach
 *
 */
public class HTMLDocumentResponse {

	BufferedWriter obuf;
	
	public HTMLDocumentResponse(int httpStatusCode, IResponseFactory f, byte[] response) throws ServerException, IOException {
		
		ResponseHeaders hdrs = new ResponseHeaders();
		hdrs.addHeader("Content-Type", "text/html; charset=utf-8");
		hdrs.setContentLength(response.length);
		
		IResponse resp = f.createResponse(httpStatusCode, hdrs);
		
		resp.getResponseStream().write(response);
	}
	
	
	
}
