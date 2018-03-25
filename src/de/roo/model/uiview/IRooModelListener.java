package de.roo.model.uiview;


/**
 * 
 * @author Leo Nobach
 *
 */
public interface IRooModelListener {

	public void resourceChanged(IRooModel mdl, IRooResource res, int index);
	
	public void resourceAdded(IRooModel mdl, IRooResource res, int index);
	
	public void resourceRemoved(IRooModel mdl, IRooResource res, int index);
	
}
