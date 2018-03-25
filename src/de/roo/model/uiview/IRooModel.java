package de.roo.model.uiview;

import java.io.File;
import java.util.List;

import de.roo.logging.ILog;

public interface IRooModel {

	public abstract IRooResource getResource(String identifier);

	public abstract IRooResource getResourceAt(int index);

	public abstract void remove(Object res, ILog log);

	public abstract void addListener(IRooModelListener l);

	public abstract void removeListener(IRooModelListener l);

	public abstract int getNumResources();

	public abstract List<? extends IRooResource> getResourcesList();

	public abstract int getResourceIndex(Object res);
	
	public IRooDownloadResource addNewDownloadResource();
	
	public IRooUploadResource addNewUploadResource(File f);

}