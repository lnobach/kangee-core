package de.roo.httpsrv;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.roo.http.ReqMethod;
import de.roo.http.Version;
import de.roo.srvApi.IPOSTedFile;
import de.roo.srvApi.IRequest;
import de.roo.srvApi.IRequesterInfo;
import de.roo.srvApi.ServerException;

/**
 * 
 * @author Leo Nobach
 *
 */
public class DefaultRequest implements IRequest {

	Map<String, String> headers;
	
	ReqMethod method;
	String reqPath;
	Version version;

	private RequesterInfo requesterInfo;

	private List<IPOSTedFile> postedFiles;
	
	public DefaultRequest(String requestLine, RequesterInfo requesterInfo) throws ServerException {
		headers = new HashMap<String, String>();
		this.requesterInfo = requesterInfo;
		
		String[] lineElements = requestLine.split(" ");
		if (lineElements.length < 2) throw new ServerException("Request line does not contain enough elements");
		try {
			method = Enum.valueOf(ReqMethod.class, lineElements[0]);
		} catch (IllegalArgumentException e) {
			throw new ServerException("Request method " + lineElements[0] + " is unknown");
		}
		reqPath = lineElements[1];
		if (lineElements.length == 2) version = new Version();	//Assume HTTP/1.0 if nothing is given
		else version = new Version(lineElements[2]);
		if (version == null) throw new ServerException("Version " + lineElements[2] + " not supported");
	}
	
	@Override	
	public Map<String, String> getHeaders() {
		return Collections.unmodifiableMap(headers);
	}
	
	@Override	
	public String getHeader(String key) {
		return headers.get(key);
	}
	
	@Override
	public IRequesterInfo getRequesterInfo() {
		return requesterInfo;
	}
	
	public void parseAndAdd(String headerLine) throws ServerException {
	
		int delimiter = headerLine.indexOf(":");
		if (delimiter == -1) throw new ServerException("Header malformed: " + headerLine);
		
		String key = headerLine.substring(0, delimiter);
		String value = headerLine.substring(delimiter +1);
		
		if (value.startsWith(" ")) value = value.substring(1);
		
		headers.put(key, value);
		
	}

	@Override
	public ReqMethod getReqMethod() {
		return method;
	}

	@Override
	public String getRequestedPath() {
		return reqPath;
	}

	@Override
	public Version getVersion() {
		return version;
	}

	public void setPOSTedFiles(List<IPOSTedFile> postedFiles) {
		this.postedFiles = postedFiles;
	}

	@Override
	public List<IPOSTedFile> getPOSTedFiles() {
		return postedFiles;
	}
	
}
