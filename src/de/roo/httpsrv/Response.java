/**
 * 
 */
package de.roo.httpsrv;

import java.io.OutputStream;

import de.roo.srvApi.IResponse;
import de.roo.util.stream.CountingOutputStream;

/**
 * 
 * @author Leo Nobach
 *
 */
public class Response implements IResponse {
	
	private CountingOutputStream responseStr;

	public Response(OutputStream responseStr) {
		this.responseStr = new CountingOutputStream(responseStr);
	}

	@Override
	public OutputStream getResponseStream() {
		return responseStr;
	}
	
	public long getResponseLength() {
		return responseStr.getByteCountWritten();
	}
	
}