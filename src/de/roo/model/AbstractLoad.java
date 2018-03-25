package de.roo.model;

import java.util.ArrayList;
import java.util.List;

import de.roo.model.uiview.IAbstractLoad;
import de.roo.model.uiview.IRooResource;
import de.roo.srvApi.IRequest;

/**
 * 
 * @author Leo Nobach
 *
 */
public abstract class AbstractLoad implements IAbstractLoad {
	
	private List<IUploadListener> listeners = new ArrayList<IUploadListener>();

	/* (non-Javadoc)
	 * @see de.roo.model.IAbstractLoad#getProgress()
	 */
	@Override
	public abstract double getProgress();
	
	/* (non-Javadoc)
	 * @see de.roo.model.IAbstractLoad#getCurrentUploadAmount()
	 */
	@Override
	public abstract long getCurrentUploadAmount();
	
	/* (non-Javadoc)
	 * @see de.roo.model.IAbstractLoad#getRequiredUploadAmount()
	 */
	@Override
	public abstract long getRequiredUploadAmount();
	
	/* (non-Javadoc)
	 * @see de.roo.model.IAbstractLoad#getState()
	 */
	@Override
	public abstract LoadState getState();
	
	/* (non-Javadoc)
	 * @see de.roo.model.IAbstractLoad#getHTTPRequest()
	 */
	@Override
	public abstract IRequest getHTTPRequest();
	
	/* (non-Javadoc)
	 * @see de.roo.model.IAbstractLoad#getResource()
	 */
	@Override
	public abstract IRooResource getResource();
	
	/* (non-Javadoc)
	 * @see de.roo.model.IAbstractLoad#addListener(de.roo.model.AbstractLoad.IUploadListener)
	 */
	@Override
	public void addListener(IUploadListener l) {
		listeners.add(l);
	}
	
	/* (non-Javadoc)
	 * @see de.roo.model.IAbstractLoad#removeListener(de.roo.model.AbstractLoad.IUploadListener)
	 */
	@Override
	public void removeListener(IUploadListener l) {
		listeners.remove(l);
	}
	
	protected void stateChanged() {
		for (IUploadListener l : listeners) l.stateChanged(this);
	}

	/* (non-Javadoc)
	 * @see de.roo.model.IAbstractLoad#terminate()
	 */
	@Override
	public abstract void terminate();
	
	/* (non-Javadoc)
	 * @see de.roo.model.IAbstractLoad#hasFinished()
	 */
	@Override
	public boolean hasFinished() {
		LoadState s = getState();
		if (s == LoadState.CANCELLED) return true;
		if (s == LoadState.FAILED) return true;
		if (s == LoadState.SUCCESS) return true;
		return false;
		
	}
	
}
