package de.roo.model.uiview;

import java.io.File;

import de.roo.logging.ILog;

public interface IRooDownloadResource extends IRooResource {

	public enum DLState {
		NoFileUploaded,
		FileUploaded, 
		FileUploadError;
	}
	
	public abstract boolean canSave();

	public abstract DLState getDLState();

	public abstract boolean downloadBlocked();

	public abstract void deleteFile(ILog log);

	public abstract void saveResourceTo(File target, ILog log);

}