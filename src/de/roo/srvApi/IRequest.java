package de.roo.srvApi;

import java.util.List;
import java.util.Map;

import de.roo.http.ReqMethod;
import de.roo.http.Version;

/**
 * 
 * @author Leo Nobach
 *
 */
public interface IRequest {

	public Map<String, String> getHeaders();
	
	public String getHeader(String key);
	
	public ReqMethod getReqMethod();
	
	public String getRequestedPath();
	
	public Version getVersion();
	
	public List<IPOSTedFile> getPOSTedFiles();

	IRequesterInfo getRequesterInfo();
	
}
