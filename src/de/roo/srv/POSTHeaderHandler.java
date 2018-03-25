package de.roo.srv;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.roo.logging.ILog;

/**
 * 
 * @author Leo Nobach
 *
 */
public class POSTHeaderHandler {

	static Pattern cDispP = Pattern.compile("\\G\\s*([\\w-_]+)\\s*(=\\s*\"([^\"]*)\"\\s*)?;?");
	private ILog log;
	private String contentDisposition;
	private String fileName;
	private String fieldName;
	private String mimeType;

	public POSTHeaderHandler(Map<String, String> header, ILog log) {
		this.log = log;
		parseHeader(header);
	}

	private void parseHeader(Map<String, String> header) {
		parseContentDisposition(header.get("Content-Disposition"));
		mimeType = header.get("Content-Type");
	}


	protected void parseContentDisposition(String cDispStr) {
		Matcher m = cDispP.matcher(cDispStr);
		Map<String, String> fields = new HashMap<String, String>();
		
		if (m.find()) {
			contentDisposition = m.group(1);
			if (!"form-data".equals(contentDisposition)) log.warn(this, "Content-Disposition is not 'form-data': "
					+ contentDisposition + " Complete Content-Disposition header is: '" + cDispStr + "'. Trying to go on.");
			
			while (m.find()) {
				String name = m.group(1);
				String value; 
				try {
					value = m.group(3);
				} catch (IndexOutOfBoundsException e) {
					value = null;
				}
				
				fields.put(name, value);
			}
			fileName = fields.get("filename");
			if (fileName == null) log.warn(this, "Content-Disposition: Field 'filename' was not set. Complete Content-Disposition header is: '" + cDispStr + "'. Trying to go on.");
			fieldName = fields.get("name");
			if (fieldName == null) log.warn(this, "Content-Disposition: Field 'name' was not set. Complete Content-Disposition header is: '" + cDispStr + "'. Trying to go on.");
			
		} else
			contentDisposition = null;
		
	}
	
	public String getContentDisposition() {
		return contentDisposition;
	}

	public String getMimeType() {
		return mimeType;
	}

	public String getFileName() {
		return fileName;
	}

	public String getFieldName() {
		return fieldName;
	}
	
}
/*
Content-Disposition: form-data; name="file"; filename="Testdatei.txt" 
Content-Type: text/plain
*/