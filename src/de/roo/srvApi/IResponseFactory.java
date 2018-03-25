package de.roo.srvApi;

import java.io.IOException;

/**
 * 
 * @author Leo Nobach
 *
 */
public interface IResponseFactory {

	public IResponse createResponse(int httpStatus, ResponseHeaders hdrs) throws ServerException, IOException;
	
	public void createDefaultStatusResponse(int httpStatus) throws ServerException, IOException;
	
}
