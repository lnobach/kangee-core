package de.roo.srv;

import de.roo.logging.ILog;
import de.roo.model.AbstractLoad;
import de.roo.model.RooDownloadResource;
import de.roo.model.uiview.IDownload;
import de.roo.model.uiview.IRooDownloadResource.DLState;
import de.roo.model.uiview.IRooResource;
import de.roo.srvApi.IPOSTFileTransferInfo;
import de.roo.srvApi.IRequest;
import de.roo.srvApi.IPOSTFileTransferInfo.IPOSTFileTransferInfoListener;
import de.roo.srvApi.IPOSTFileTransferInfo.POSTFileState;

/**
 * 
 * @author Leo Nobach
 *
 */
public class Download extends AbstractLoad implements IDownload {

	private RooDownloadResource res;
	private IPOSTFileTransferInfo obs;
	private IRequest req;
	private ILog log;
	
	public Download(RooDownloadResource res, IPOSTFileTransferInfo obs, IRequest req, ILog log) {
		this.res = res;
		this.obs = obs;
		this.req = req;
		this.log = log;
		obs.addListener(this.new Listener());
		res.addLoad(this);
		stateChanged();			//Skips PREPARING step, so notify listeners.
	}

	@Override
	public long getCurrentUploadAmount() {
		if (obs.getState() == POSTFileState.Success) return obs.getFinalSize();
		return obs.getBytesRead();
	}

	@Override
	public double getProgress() {
		if (obs.getState() == POSTFileState.Success) return 1d;
		return ((double)obs.getBytesRead() / obs.getTotalBytesEstim());
	}

	@Override
	public long getRequiredUploadAmount() {
		if (obs.getState() == POSTFileState.Success) return obs.getFinalSize();
		return obs.getTotalBytesEstim();
	}

	@Override
	public LoadState getState() {
		if (obs.getState() == POSTFileState.Success) return LoadState.SUCCESS;
		if (obs.getState() == POSTFileState.Error) return LoadState.FAILED;
		if (obs.getState() == POSTFileState.Cancel) return LoadState.CANCELLED;
		if (obs.getState() == POSTFileState.No_File) return LoadState.CANCELLED;
		else return LoadState.RUNNING;
	}

	@Override
	public void terminate() {
		obs.terminate();
	}
	
	class Listener implements IPOSTFileTransferInfoListener {

		@Override
		public void stateChanged() {
			
			POSTFileState s = obs.getState();
			
			if (s == POSTFileState.Success) {
				res.setDLState(DLState.FileUploaded);
			} else if (s == POSTFileState.No_File) {
				res.setDLState(DLState.NoFileUploaded);
				log.warn(this, "Empty file was uploaded. Maybe the client didn't specify a file to upload. Removing it.");
				res.deleteFile(log);	//The POST file stream was empty. So delete the file
			} else if (s == POSTFileState.Error) {
				res.setDLState(DLState.FileUploadError);
				res.deleteFile(log);
			} else if (s == POSTFileState.Cancel) {
				res.setDLState(DLState.NoFileUploaded);
				res.deleteFile(log);
			}
			if (s != POSTFileState.Running) res.setBlocked(false);
			res.resourceChanged();
			Download.super.stateChanged();
		}
		
	}

	@Override
	public IRequest getHTTPRequest() {
		return req;
	}

	@Override
	public IRooResource getResource() {
		return res;
	}

}
