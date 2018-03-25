package de.roo.model;

import java.io.File;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import de.roo.logging.ILog;
import de.roo.model.uiview.IRooDownloadResource;
import de.roo.util.FileUtils;

/**
 * 
 * @author Leo Nobach
 *
 */
public class RooDownloadResource extends RooResource implements IRooDownloadResource {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4648911870393565012L;
	
	String filename = "Unknown";
	
	int attemptSeqNo = -1;

	private DLState dlState = DLState.NoFileUploaded;
	
	boolean blocked = false;
	
	public String toString() {
		return "RooDownloadResource (filename=" + filename + ", attemtSeqNo=" + attemptSeqNo
			+ ", dlState=" + dlState + ", blocked=" + blocked + ", identifier=" + identifier
			+ ", file=" + f + ", mimeType=" + mimeType + ")";
	}
	
	public RooDownloadResource(Element elem, ILog log) throws ModelIOException {
		super(elem, log);
		
		String stateStr = elem.getAttribute("state");
		try {
			dlState = DLState.valueOf(stateStr);
		} catch (Exception e) {
			throw new ModelIOException("Illegal state value: " + stateStr, e);
		}
		
		String attemptSeqNoStr = elem.getAttribute("attemptSeqNo");
		try {
			attemptSeqNo = Integer.parseInt(attemptSeqNoStr);
		} catch (Exception e) {
			throw new ModelIOException("Illegal attempt sequence number value: " + attemptSeqNoStr, e);
		}
		
		String path = elem.getAttribute("file");
		
		if (dlState == DLState.FileUploaded) {
			if ("".equals(path) || path == null) throw new ModelIOException("File was uploaded, but file attribute is empty.");
			File f = new File(path);
			if (!f.exists()) throw new ModelIOException("File " + path + " was uploaded, but does not exist (anymore)");
			if (!f.isFile()) throw new ModelIOException("'File' " + path + " is not a regular file");
			this.setFile(f);
		}
		
		String filename = elem.getAttribute("filename");
		if (!"".equals(filename) && filename != null) this.filename = filename;
	}

	public RooDownloadResource() {
		super();
	}

	public void setFileName(String fileName) {
		synchronized (treeLock) {
			this.filename  = fileName;
		}
	}
	
	/* (non-Javadoc)
	 * @see de.roo.model.IRooDownloadResource#canSave()
	 */
	@Override
	public boolean canSave() {
		return this.getDLState() == DLState.FileUploaded && this.getFile() != null && !this.downloadBlocked();
	}

	private int getNewAttemptSeqNo() {
		synchronized (treeLock) {
			attemptSeqNo++;
			return attemptSeqNo;
		}
		
	}
	
	public void setDLState(DLState state) {
		synchronized (treeLock) {
			this.dlState = state;
		}
	}

	/* (non-Javadoc)
	 * @see de.roo.model.IRooDownloadResource#getDLState()
	 */
	@Override
	public DLState getDLState() {
		synchronized (treeLock) {
			return dlState;
		}
	}

	/* (non-Javadoc)
	 * @see de.roo.model.IRooDownloadResource#getPlainFileName()
	 */
	@Override
	public String getPlainFileName() {
		synchronized (treeLock) {
			return filename;
		}
	}

	/* (non-Javadoc)
	 * @see de.roo.model.IRooDownloadResource#downloadBlocked()
	 */
	@Override
	public boolean downloadBlocked() {
		synchronized (treeLock) {
			return blocked;
		}
	}
	
	public void setBlocked(boolean blocked) {
		synchronized (treeLock) {
			this.blocked = blocked;
		}
	}

	/* (non-Javadoc)
	 * @see de.roo.model.IRooDownloadResource#deleteFile(de.roo.logging.ILog)
	 */
	@Override
	public void deleteFile(ILog log) {
		synchronized (treeLock) {
			log.dbg(this, "Deleting temporary file " + f + " from resource " + this);
			File f = this.getFile();
			if (f != null) {
				f.delete();
				f.getParentFile().delete();	//dlBaseDir is being deleted also.
			}
			this.setFile(null);
		}
	}
	
	public File createFile(String proposedFilename, File postFilesBasedir) {
		synchronized (treeLock) {
			String filename = FileUtils.replaceIllegalFileNameChars(proposedFilename);
			if ("".equals(filename.trim())) filename = null;
			this.setFileName(filename);
			File f = getNewTMPFile(filename, postFilesBasedir);
			this.setFile(f);
			return f;
		}
	}
	

	private File getNewTMPFile(String filename, File postFilesBasedir) {
		File dlBaseDir = new File(postFilesBasedir, getIdentifier() + "_" + getNewAttemptSeqNo());
		dlBaseDir.mkdirs();
		File result = new File(dlBaseDir, filename==null?"unknown":filename);
		return result;
	}
	
	/* (non-Javadoc)
	 * @see de.roo.model.IRooDownloadResource#saveResourceTo(java.io.File, de.roo.logging.ILog)
	 */
	@Override
	public void saveResourceTo(File target, ILog log) {
		synchronized (treeLock) {
			File f = this.getFile();
			FileUtils.moveFile(f, target, log);
			f.getParentFile().delete();	//dlBaseDir is being deleted also.
			f = null;
			this.getParent().remove(this, log);
		}
	}
	
	public void saveState(Element rootNode, Document fact, ILog log) throws ModelIOException {
		super.saveState(rootNode, fact, log);
		rootNode.setAttribute("type", "download");
		if (filename != null) rootNode.setAttribute("filename", filename);
		rootNode.setAttribute("state", dlState.toString());
		rootNode.setAttribute("attemptSeqNo", String.valueOf(attemptSeqNo));
	}
	
	@Override
	public void cleanupOnRemove(ILog log) {
		if (this.getFile() != null) deleteFile(log);
	}

}
