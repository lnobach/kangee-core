package de.roo.httpcli;

import java.io.IOException;

/**
 * 
 * @author Leo Nobach
 *
 */
public interface IResponseHandler {

	public void handleResponse(HttpResponse resp) throws IOException;
	
}
