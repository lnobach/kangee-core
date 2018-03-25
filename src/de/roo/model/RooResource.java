package de.roo.model;

import java.io.File;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import de.roo.logging.ILog;
import de.roo.model.uiview.IAbstractLoad;
import de.roo.model.uiview.IRooResource;
import de.roo.util.StringHasher;
import de.roo.util.StringToolkit;
import de.roo.util.server.MIMETypes;

/**
 * 
 * @author Leo Nobach
 *
 */
public abstract class RooResource implements IXMLSavable, IRooResource {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3270759358972483279L;

	public static final int HASH_LENGTH = 10;
	
	String identifier;
	
	Object treeLock = new Object();
	
	transient List<AbstractLoad> loads;
	transient List<IResourceListener> listeners;
	
	RooModel parent = null;
	
	File f = null;

	protected String mimeType = null;
	
	public RooResource() {
		this.identifier = StringHasher.hash(HASH_LENGTH);
		init();
	}
	
	public RooResource(Element elem, ILog log) throws ModelIOException {
		this.identifier = elem.getAttribute("identifier");
		if ("".equals(identifier) || identifier == null) throw new ModelIOException("Identifier for resource was not given");
		
		String mimeType = elem.getAttribute("mimeType");
		if (!"".equals(mimeType)) this.mimeType = mimeType;
		init();
	}

	/* (non-Javadoc)
	 * @see de.roo.model.IRooResource#getParent()
	 */
	@Override
	public RooModel getParent() {
		return parent;
	}
	
	public void setParent(RooModel parent) {
		this.parent = parent;
	}

	public void init() {
		loads = new LinkedList<AbstractLoad>();
		listeners = new LinkedList<IResourceListener>();
	}
	
	/* (non-Javadoc)
	 * @see de.roo.model.IRooResource#getFile()
	 */
	@Override
	public File getFile() {
		synchronized (treeLock) {
			return f;
		}
	}
	
	public void setFile(File f) {
		synchronized (treeLock) {
			this.f = f;
		}
	}
	
	/* (non-Javadoc)
	 * @see de.roo.model.IRooResource#getIdentifier()
	 */
	@Override
	public String getIdentifier() {
		synchronized (treeLock) {
			return identifier;
		}
	}
	
	@Override
	public abstract String getPlainFileName();
	
	/**
	 * Called e.g. from the model.
	 */
	public void resourceRemoved() {
		synchronized (treeLock) {
			for (IResourceListener l : listeners) l.resourceRemoved(this);
		}
	}
	
	/**
	 * Must be EXPLICITLY called from external resource changers.
	 */
	public void resourceChanged() {
		synchronized (treeLock) {
			for (IResourceListener l : listeners) l.resourceChanged(this);
			RooModel mdl = getParent();
			if (mdl != null) mdl.resourceChanged(this);
		}
	}
	
	void loadAdded(IAbstractLoad upl, int idx) {
		synchronized (treeLock) {
			for (IResourceListener l : listeners) l.loadAdded(this, upl, idx);
			RooModel mdl = getParent();
			if (mdl != null) mdl.resourceChanged(this);
		}
	}
	
	void loadRemoved(IAbstractLoad upl, int idx) {
		synchronized (treeLock) {
			for (IResourceListener l : listeners) l.loadRemoved(this, upl, idx);
			RooModel mdl = getParent();
			if (mdl != null) mdl.resourceChanged(this);
		}
	}
	
	public synchronized void addLoad(AbstractLoad upload) {
		synchronized (treeLock) {
			loads.add(upload);
			loadAdded(upload, loads.size() -1);
		}
	}
	
	/* (non-Javadoc)
	 * @see de.roo.model.IRooResource#removeLoad(de.roo.model.uiview.IAbstractLoad)
	 */
	@Override
	public synchronized void removeLoad(IAbstractLoad upload) {
		synchronized (treeLock) {
			int idx = loads.indexOf(upload);
			loads.remove(idx);
			loadRemoved(upload, idx);
		}
	}
	
	/* (non-Javadoc)
	 * @see de.roo.model.IRooResource#addResourceListener(de.roo.model.RooResource.IResourceListener)
	 */
	@Override
	public void addResourceListener(IResourceListener l) {
		synchronized (treeLock) {
			listeners.add(l);
		}
	}
	
	/* (non-Javadoc)
	 * @see de.roo.model.IRooResource#removeResourceListener(de.roo.model.RooResource.IResourceListener)
	 */
	@Override
	public void removeResourceListener(IResourceListener l) {
		synchronized (treeLock) {
			listeners.remove(l);
		}
	}

	/* (non-Javadoc)
	 * @see de.roo.model.IRooResource#getNumLoads()
	 */
	@Override
	public int getNumLoads() {
		return loads.size();
	}

	/* (non-Javadoc)
	 * @see de.roo.model.IRooResource#getLoadAt(int)
	 */
	@Override
	public IAbstractLoad getLoadAt(int index) {
		return loads.get(index);
	}

	/* (non-Javadoc)
	 * @see de.roo.model.IRooResource#getLoadsList()
	 */
	@Override
	public List<AbstractLoad> getLoadsList() {
		return Collections.unmodifiableList(loads);
	}
	
	
	/* (non-Javadoc)
	 * @see de.roo.model.IRooResource#getHttpFileName()
	 */
	@Override
	public String getHttpFileName() {
		return StringToolkit.encodeURL(getPlainFileName());
	}

	/* (non-Javadoc)
	 * @see de.roo.model.IRooResource#getMIMEType()
	 */
	@Override
	public String getMIMEType() {
		synchronized (treeLock) {
			if (mimeType == null) return MIMETypes.mimeTypeFromExt(getPlainFileName());
			else return mimeType;
		}
	}
	
	public void setMIMEType(String mimeType) {
		synchronized (treeLock) {
			this.mimeType = mimeType;
		}
	}
	
	public void saveState(Element rootNode, Document fact, ILog log) throws ModelIOException {
		rootNode.setAttribute("identifier", identifier);
		if (f != null) rootNode.setAttribute("file", f.getAbsolutePath());
		rootNode.setAttribute("mimeType", mimeType==null?"":mimeType);
	}

	public static RooResource fromDOM(Element elem, ILog log) throws ModelIOException {
		String type = elem.getAttribute("type");
		if ("upload".equals(type)) return new RooUploadResource(elem, log);
		if ("download".equals(type)) return new RooDownloadResource(elem, log);
		throw new ModelIOException("Resource in element " + elem + " is neither 'upload' nor 'download'.");
	}

	public void cleanupOnRemove(ILog log) {
		//Nothing to do here
	}
	
}
