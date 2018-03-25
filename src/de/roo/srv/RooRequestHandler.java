package de.roo.srv;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.roo.BuildConstants;
import de.roo.configuration.IConf;
import de.roo.http.ReqMethod;
import de.roo.icons.IRooIcon;
import de.roo.icons.IRooMultiScaleIcon;
import de.roo.icons.Icons2;
import de.roo.logging.ILog;
import de.roo.model.RooDownloadResource;
import de.roo.model.RooModel;
import de.roo.model.RooResource;
import de.roo.model.RooUploadResource;
import de.roo.model.uiview.IRooDownloadResource;
import de.roo.model.uiview.IRooDownloadResource.DLState;
import de.roo.model.uiview.IRooResource;
import de.roo.srvApi.BadPOSTException;
import de.roo.srvApi.IPOSTFileTransferInfo;
import de.roo.srvApi.IRequest;
import de.roo.srvApi.IRequestHandler;
import de.roo.srvApi.IResponse;
import de.roo.srvApi.IResponseFactory;
import de.roo.srvApi.IServerContext;
import de.roo.srvApi.ResponseHeaders;
import de.roo.srvApi.ServerException;
import de.roo.themes.ITheme;
import de.roo.themes.ITheme.TemplateType;
import de.roo.themes.Themes;
import de.roo.util.server.HTTPStringTools;
import de.roo.util.stream.StreamCopy;

/**
 * 
 * @author Leo Nobach
 *
 */
public class RooRequestHandler implements IRequestHandler {

	static final String THEMES_FOLDER = "/themes/";
	
	static final boolean DBG_INDEX = false;
	
	Pattern downloadMatcher = Pattern.compile("/(\\w{" + RooResource.HASH_LENGTH + "}+)/upload");
	
	private RooModel mdl;
	private ILog log;

	private IConf conf;
	private File appBaseDir;
	private Icons2 icons;

	private String rooID;
	
	byte[] favicon = null;	//lazy load

	/**
	 * If mdl is null, no resources can be requested.
	 * @param mdl
	 * @param log
	 */
	public RooRequestHandler(RooModel mdl, IConf conf, ILog log, File appBaseDir, Icons2 icons, String rooID) {
		this.mdl = mdl;
		this.log = log;
		this.conf = conf;
		this.appBaseDir = appBaseDir;
		this.icons = icons;
		this.rooID = rooID;
	}
	
	@Override
	public void handleRequest(IRequest request, IResponseFactory f, IServerContext ctx)
			throws ServerException, IOException {

		String reqPath = request.getRequestedPath();
		ReqMethod method = request.getReqMethod();
		ITheme currentTheme = getTheme();
		
		if (!reqPath.startsWith("/")) {
			notfound(f);
			return;
		}
		
		if ("/ping".equals(reqPath)) {
			answerPong(f);
			return;
		}
		
		if ("/favicon.ico".equals(reqPath)) {
			answerFavicon(f);
			return;
		}
		
		if (reqPath.startsWith(THEMES_FOLDER)) {
			if (method == ReqMethod.GET) {
				//Load theme resource
				
				String rest = reqPath.substring(THEMES_FOLDER.length());
				int delimiterPos = rest.indexOf("/");
				if (delimiterPos == -1) {
					log.dbg(this, "Client has requested " + "/" + " which is not a valid theme resource.");
					notfound(f);
					return;
				}
				String themeName = rest.substring(0, delimiterPos);
				String themeResourceName = rest.substring(delimiterPos +1);
				
				
				if (Themes.getDesc(currentTheme).key().equals(themeName)) {
					log.dbg(this, "Client has requested a theme resource, answering");
					answerThemeResource(themeResourceName, currentTheme, f);
					return;
				}
			} else {
				unsupported(f);
				return;
			}
		} else if (mdl != null) {
			if (method == ReqMethod.GET || method == ReqMethod.POST) {
				if (reqPath.length() > RooResource.HASH_LENGTH) {
					String identifier = reqPath.substring(1, RooResource.HASH_LENGTH+1);
					RooResource res = mdl.getResource(identifier);
					if (res != null) {
						log.dbg(this, "Client has requested a path of file " + res + ", answering");
						String pathTail = reqPath.length() > RooResource.HASH_LENGTH+1?
								reqPath.substring(RooResource.HASH_LENGTH+2):null;
						handleResourcePath(res, pathTail, request, f, currentTheme);
						return;
					}
				}
			} else {
				unsupported(f);
				return;
			}
		}
		log.dbg(this, "Client has requested " + reqPath + " which is not available.");
		noteAntiBF(ctx, request, reqPath);
		notfound(f);
	}
	
	private void answerFavicon(IResponseFactory f) throws IOException, ServerException {
		if (favicon == null) {
			InputStream res = RooRequestHandler.class.getResourceAsStream("/de/roo/gfx/favicon.ico");
			if (res == null) {
				log.warn(this, "Favicon was not found on request. Omitting.");
				notfound(f);
				return;
			}
			InputStream is = new BufferedInputStream(res);
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			new StreamCopy().copy(is, os);
			favicon = os.toByteArray();
			res.close();
			log.dbg(this, "Favicon loaded. Size: " + favicon.length + " bytes");
		}
		
		log.dbg(this, "Client has requested favicon.ico");
		ResponseHeaders hdrs = new ResponseHeaders();
		hdrs.addHeader("Content-Type", "image/x-icon");
		hdrs.setContentLength(favicon.length);
		IResponse resp = f.createResponse(200, hdrs);
		OutputStream respStr = resp.getResponseStream();
		
		respStr.write(favicon);
		respStr.flush();
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

	/*
	private void answerDebugIndex(IResponseFactory f) throws IOException, ServerException {
		HTMLDocumentResponse resp = new HTMLDocumentResponse(200, f);
		BufferedWriter writer = resp.getDocumentWriter();
		writer.write("<H1>Debug Index</H1><ul>\r\n");
		
		for (RooResource resource : mdl.getResourcesList()) {
			writer.write("  <li><a href=\"/" + resource.getIdentifier() + "\">" + resource + "</a>\r\n");
		}
		writer.write("</ul>");
		writer.flush();
		
	}
	*/

	private void answerThemeResource(String resourceIdentifier, ITheme theme, IResponseFactory f) throws IOException, ServerException {
		theme.answerTemplateResource(resourceIdentifier, f);
	}

	private void notfound(IResponseFactory f) throws ServerException, IOException {
		f.createDefaultStatusResponse(404);
	}
	
	private void unsupported(IResponseFactory f) throws ServerException, IOException {
		f.createDefaultStatusResponse(405);
	}

	private void handleResourcePath(RooResource res, String pathTail,
			IRequest request, IResponseFactory f, ITheme currentTheme) throws ServerException, IOException {
		
		ReqMethod method = request.getReqMethod();
		
		if (pathTail == null || "".equals(pathTail)) {
			if (method == ReqMethod.GET) {
				TemplateType type;
				if (res instanceof RooDownloadResource) {
					IRooDownloadResource dlRes = (IRooDownloadResource)res;
					type = (dlRes.downloadBlocked() || dlRes.getDLState() == DLState.FileUploaded)?TemplateType.Status:TemplateType.Download;
				} else {
					type = TemplateType.Upload;
				}
				currentTheme.answerStartDocumentFor(res, f, type, conf);
			} else {
				unsupported(f);
				return;
			}
		} else if (pathTail.equals("icon")) {
			if (method == ReqMethod.GET) {
				answerIcon(res, request, f);
			} else {
				unsupported(f);
				return;
			}
		} else if (res instanceof RooUploadResource) {
			if (method == ReqMethod.GET) {
				RooUploadResource upRes = (RooUploadResource)res;
				if (HTTPStringTools.equalsInURL(upRes.getHttpFileName(), pathTail)) {
					answerUploadFor(upRes, f, request);
				} else {
					notfound(f);
				}
			} else {
				unsupported(f);
				return;
			}
		} else {
			if (method == ReqMethod.POST && pathTail.equals("upload") && res instanceof RooDownloadResource) {
				answerDownloadFor((RooDownloadResource) res, f, currentTheme);
				return;
			}
			notfound(f);
		}
	}

	private void answerIcon(IRooResource res, IRequest request,
			IResponseFactory f) throws ServerException, IOException {
		IRooMultiScaleIcon iconScl = icons.getIconForFile(res.getFile());
		IRooIcon icon = iconScl.getIconSmallerEqual(Icons2.S_48x48);
		
		byte[] iconBts = icon.getBytes();
		
		if (iconBts == null) notfound(f);
		else {
			ResponseHeaders hdrs = new ResponseHeaders();
			hdrs.setContentLength(iconBts.length);
			hdrs.addHeader("Content-Type", icon.getImageMimeType());
			IResponse resp = f.createResponse(200, hdrs);
			resp.getResponseStream().write(iconBts);
		}
	}

	private void answerUploadFor(RooUploadResource res, IResponseFactory f, IRequest req) throws ServerException {
		new Upload(res, f, req, log);
	}
	
	private void answerDownloadFor(RooDownloadResource res, IResponseFactory f, ITheme currentTheme) throws ServerException, IOException {
		currentTheme.answerStartDocumentFor(res, f, TemplateType.Status, conf);
	}

	@Override
	public File getFileNameForPOSTContent(IRequest req,
			Map<String, String> partHeaders, IPOSTFileTransferInfo info, IServerContext ctx) throws BadPOSTException {
		
		Matcher m = downloadMatcher.matcher(req.getRequestedPath());
		if (!m.matches()) throw new BadPOSTException("Malformed POST path: " + req.getRequestedPath());
		String resID = m.group(1);
		IRooResource res = mdl.getResource(resID);
		if (res == null) {
			noteAntiBF(ctx, req, req.getRequestedPath());
			throw new BadPOSTException("No resource with the given ID " + resID + " found.");
		}
		if (!(res instanceof RooDownloadResource)) 
			throw new BadPOSTException("Resource with the given ID " + resID + " is not a Download resource");
		RooDownloadResource dlRes = (RooDownloadResource)res;
		
		if (dlRes.downloadBlocked() || dlRes.getDLState() == DLState.FileUploaded) {
			log.warn(this, "File was already, or is currently being downloaded.");
			return null;
		}
		
		POSTHeaderHandler hdlr = new POSTHeaderHandler(partHeaders, log);
		String fName = hdlr.getFileName();
		if (fName == null || "".equals(fName.trim())) {
			log.warn(this, "User has left the form data field 'file' empty.");
			return null;
		}
		File tmpFile = dlRes.createFile(fName, getPostFilesBasedir());
		dlRes.setBlocked(true);
		setFileAttributes(dlRes, tmpFile, hdlr);
		new Download(dlRes, info, req, log);
		return tmpFile;
	}
	
	void noteAntiBF(IServerContext ctx, IRequest req, String reqPath) {
		ctx.getAntiBF().noteFailedAttemptFrom(req.getRequesterInfo().getRequesterIP(), reqPath);
	}
	
	
	private void setFileAttributes(RooDownloadResource res, File tmpFile, POSTHeaderHandler hdlr) {
		res.setMIMEType(hdlr.getMimeType());
		if (!"file".equals(hdlr.getFieldName())) log.warn(this, "POST field name is not 'file'. Trying to go on.");
		res.setFile(tmpFile);
		res.resourceChanged();
	}
	
	static final String POST_FILES_BASEDIR = "temp";
	
	public File getPostFilesBasedir() {
		String postFilesBasedir = conf.getValueString("POST_Files_Basedir", "auto");
		if (!"auto".equals(postFilesBasedir)) return new File(postFilesBasedir);
		return new File(appBaseDir, "TemporaryResources");
	}
	
	
	private ITheme getTheme() throws ServerException {
		String themeStr = conf.getValueString("Presentation_Template", BuildConstants.DEFAULT_PRESENTATION_THEME);
		ITheme theme = Themes.getThemesAsMap().get(themeStr);
		if (theme == null) {
			log.warn(this, "Could not find selected theme " + themeStr
					+ ". Falling back to default theme " + BuildConstants.DEFAULT_PRESENTATION_THEME);
			theme = Themes.getThemesAsMap().get(BuildConstants.DEFAULT_PRESENTATION_THEME);
			if (theme == null) throw new ServerException("Could not find theme " + themeStr
					+ " and even not the default theme " + BuildConstants.DEFAULT_PRESENTATION_THEME +
					"No HTML presentation is possible. We can not continue.");
		}
		return theme;
	}

}





