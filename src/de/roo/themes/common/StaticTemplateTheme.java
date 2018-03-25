package de.roo.themes.common;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.roo.configuration.IConf;
import de.roo.model.RooResource;
import de.roo.srvApi.IResponse;
import de.roo.srvApi.IResponseFactory;
import de.roo.srvApi.ResponseHeaders;
import de.roo.srvApi.ServerException;
import de.roo.themes.ITheme;
import de.roo.util.FileUtils;
import de.roo.util.server.MIMETypes;
import de.roo.util.stream.StreamCopy;

/**
 * 
 * @author Leo Nobach
 *
 */
public abstract class StaticTemplateTheme implements ITheme {

	Pattern p = Pattern.compile("\\[--roo:(\\w+)--\\]");
	
	Set<String> resources = new HashSet<String>();

	@Override
	public void answerStartDocumentFor(RooResource res, IResponseFactory f, TemplateType type, IConf conf)
			throws IOException, ServerException {

		String tplName = getStaticRootTemplate(type);
		InputStream docIS = this.getClass().getResourceAsStream(tplName);
		if (docIS == null) throw new ServerException("No template with the name " + tplName + " was found.");
		ResponseHeaders hdrs = new ResponseHeaders();
		
		byte[] responseContent = this.copyWithReplacements(docIS, res, type, conf).getBytes();
		hdrs.setContentLength(responseContent.length);
		IResponse resp = f.createResponse(200, hdrs);
		OutputStream os = resp.getResponseStream();
		os.write(responseContent);
		os.flush();
	}

	private String copyWithReplacements(InputStream is, 
			RooResource res, TemplateType type, IConf conf) throws IOException {
		InputStreamReader reader = new InputStreamReader(is);
		BufferedReader buf = new BufferedReader(reader);
		StringBuilder str = new StringBuilder();
		
		String line;
		while ((line=buf.readLine()) != null) {
			str.append(replaceLine(line, res, type, conf) + "\r\n");
		}
		return str.toString();
	}

	@Override
	public void answerTemplateResource(String resourceName, IResponseFactory f)
			throws IOException, ServerException {
		if (resources.contains(resourceName)) {			//Keep check for legal resources for security reasons!
			InputStream str = this.getClass().getResourceAsStream(resourceName);
			ByteArrayOutputStream resBuf = new ByteArrayOutputStream();
			new StreamCopy().copy(str, resBuf);
			byte[] resource = resBuf.toByteArray();
			
			String extName = FileUtils.getFileExtension(resourceName);
			String mimeType = MIMETypes.mimeTypeFromExt(extName);	//eventuell besser machen
			
			ResponseHeaders hdrs = new ResponseHeaders();
			hdrs.addHeader("Content-Type", mimeType);
			hdrs.setContentLength(resource.length);
			
			IResponse resp = f.createResponse(200, hdrs);
			OutputStream respStr = resp.getResponseStream();
			
			respStr.write(resource);
			
			str.close();
			respStr.flush();
			return;
		}
		f.createDefaultStatusResponse(404);
	}

	protected abstract String getStaticRootTemplate(TemplateType type);

	public String replaceLine(String line, RooResource res, TemplateType type, IConf conf) {

		StringBuffer result = new StringBuffer();

		Matcher m = p.matcher(line);

		int lastMatchEnd = 0;
		while (m.find()) {
			String codeGroup = m.group(1);
			
			int start = m.start();
			result.append(line.substring(lastMatchEnd, start));
			lastMatchEnd = m.end();

			String processed = getVariableContent(codeGroup.trim(), res, type, conf);
			if (processed == null) processed = "";
			result.append(processed);
		}
		result.append(line.substring(lastMatchEnd));
		return result.toString();
		
	}
	
	protected void addResource(String resource) {
		resources.add(resource);
	}

	protected abstract String getVariableContent(String variableID, RooResource res, TemplateType type, IConf conf);

	@Override
	public InputStream getPreviewImage() {
		return this.getClass().getResourceAsStream(getPreviewImageFilename());
	}

	protected abstract String getPreviewImageFilename();
	
}
