package de.roo.srv;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.SocketException;

import de.roo.logging.ILog;
import de.roo.model.AbstractLoad;
import de.roo.model.RooUploadResource;
import de.roo.model.uiview.IUpload;
import de.roo.srvApi.CancelledException;
import de.roo.srvApi.IRequest;
import de.roo.srvApi.IResponse;
import de.roo.srvApi.IResponseFactory;
import de.roo.srvApi.ResponseHeaders;
import de.roo.srvApi.ServerException;
import de.roo.util.FileUtils;
import de.roo.util.NumberFormatToolkit;
import de.roo.util.server.MIMETypes;
import de.roo.util.stream.StreamCopy;

/**
 * 
 * @author Leo Nobach
 *
 */
public class Upload extends AbstractLoad implements IUpload {

	static final String DEFAULT_CONTENT_TYPE = "application/octet-stream";
	
	private RooUploadResource res;
	//private IResponseFactory f;
	private LoadState state = LoadState.PREPARING;
	private IRequest req;

	StreamCopy copy;

	private ILog log;
	
	boolean terminated = false;
	
	long fileLength = -1;
	
	RangeHandler hdlr;

	public Upload(RooUploadResource res, IResponseFactory f, IRequest req, ILog log) throws ServerException {
		super();
		
		this.res = res;
		//this.f = f;
		this.req = req;
		this.log = log;
		boolean cancelled = false;
		
		try {
		
			ResponseHeaders hdrs = prepare();
			if (hdrs == null) {
				f.createDefaultStatusResponse(404); 	//Not found.
				state = LoadState.FAILED;
				stateChanged();
				return;
			}
			
			hdlr = new RangeHandler(req, log, res.getFile());
			hdlr.createRangeRespHdrs(hdrs);
			
			IResponse resp = f.createResponse(hdlr.isRangeUsed()?206:200, hdrs);
				//Partial Content vs. OK
			res.addLoad(this);
				
			state = LoadState.RUNNING;
			stateChanged();
			
			
			
			if (streamFile(resp)) {
				state = LoadState.SUCCESS;
				log.dbg(this, "Upload finished successfully for " + this);
				stateChanged();
			} else {
				log.dbg(this, "Upload cancelled " + this);
				state = LoadState.CANCELLED;
				stateChanged();
				cancelled = true;
			}
			
		} catch (SocketException e) {
			String msg = e.getMessage();
			if (msg != null && msg.toLowerCase().contains("broken pipe")) {
				log.warn(this, "Socket reset. Client may have terminated the download.");
				state = LoadState.CANCELLED;
				stateChanged();
				throw new CancelledException("Connection reset by peer");
			}
			finishFailed(e);
			throw new ServerException("Error while uploading", e);
		} catch (IOException ex) {
			finishFailed(ex);
			throw new ServerException("Error while uploading", ex);
		} catch (ServerException e) {
			finishFailed(e);
			throw new ServerException("Error while uploading", e);
		}
		
		if (cancelled) throw new CancelledException("Cancelled by user.");
	}
	
	void finishFailed(Exception ex) {
		log.warn(this, "Terminating upload because of an error.", ex);
		state = LoadState.FAILED;
		stateChanged();
	}

	private ResponseHeaders prepare() {
		log.dbg(this, "Preparing headers for " + this);
		
		File f = res.getFile();
		if (!(f.exists() && f.isFile())) return null;
		
		ResponseHeaders hdrs = new ResponseHeaders();
		
		String name = f.getName();
		hdrs.addHeader("Content-Disposition", "attachment; filename=\"" + name + "\"");
		
		String extension = FileUtils.getFileExtension(f);
		String contentType = MIMETypes.mimeTypeFromExt(extension);
		if (contentType == null) contentType = DEFAULT_CONTENT_TYPE;
		hdrs.addHeader("Content-Type", contentType);
		
		return hdrs;
	}
	
	private boolean streamFile(IResponse response) throws IOException {
		
		log.dbg(this, "Stream started for " + this);
		
		InputStream is = hdlr.getRangedStream();		
		
		OutputStream os = response.getResponseStream();
		
		copy = new StreamCopy();
		boolean notTerminated = copy.copy(is, os);
		copy = null;
	    
		is.close();
		os.flush();
		return notTerminated;
	}

	@Override
	public double getProgress() {
		return getCurrentUploadAmount()/(double)getRequiredUploadAmount();
	}

	@Override
	public LoadState getState() {
		return state;
	}
	
	public String toString() {
		return "UL:" + res + "," + state + "," + NumberFormatToolkit.formatPercentage(getProgress(), 1);
	}

	@Override
	public long getCurrentUploadAmount() {
		if (copy != null) return copy.getBytesCopied();
		if (state == LoadState.SUCCESS) return hdlr.getPartialContentLength();
		return 0;
		
	}

	@Override
	public long getRequiredUploadAmount() {
		if (hdlr == null) return -1;
		return hdlr.getPartialContentLength();
	}

	@Override
	public void terminate() {
		if (copy != null) copy.terminate();
	}

	@Override
	public IRequest getHTTPRequest() {
		return req;
	}

	@Override
	public RooUploadResource getResource() {
		return res;
	}

}
