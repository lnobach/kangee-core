package de.roo.model.uiview;

import de.roo.srvApi.IRequest;

public interface IAbstractLoad {

	public enum LoadState {
		PREPARING,
		RUNNING,
		FAILED,
		CANCELLED,
		SUCCESS;
	}
	
	public static interface IUploadListener {	
		public void stateChanged(IAbstractLoad upload);
	}
	
	/**
	 * Only valid during State RUNNING
	 * @return
	 */
	public abstract double getProgress();

	public abstract long getCurrentUploadAmount();

	public abstract long getRequiredUploadAmount();

	public abstract LoadState getState();

	public abstract IRequest getHTTPRequest();

	public abstract IRooResource getResource();

	public abstract void addListener(IUploadListener l);

	public abstract void removeListener(IUploadListener l);

	public abstract void terminate();

	public abstract boolean hasFinished();

}