package de.roo.model;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import de.roo.logging.ILog;
import de.roo.model.uiview.IRooModel;
import de.roo.model.uiview.IRooModelListener;
import de.roo.model.uiview.IRooResource;
import de.roo.util.DOMToolkit;

/**
 * 
 * @author Leo Nobach
 *
 */
public class RooModel implements IXMLSavable, IRooModel {
	
	List<RooResource> resources = new LinkedList<RooResource>();
	Map<String, RooResource> idToResources = new HashMap<String, RooResource>();
	
	public RooModel() {
		//Nothing to do
	}
	
	public RooModel(Element rootElem, ILog log) throws ModelIOException {
		List<Element> elements = DOMToolkit.getAllChildElemsMatching(rootElem, "Resource", false);
		for (Element elem : elements) {
			try {
				RooResource res = RooResource.fromDOM(elem, log);
				res.setParent(this);
				resources.add(res);
				idToResources.put(res.getIdentifier(), res);
			} catch (Exception e) {
				log.warn(this, "Could not recreate resource.", e);
			}
		}
	}
	
	private synchronized void addResource(RooResource res) {
		res.setParent(this);
		resources.add(res);
		idToResources.put(res.getIdentifier(), res);
		int lastIdx = resources.size() -1;
		resourceAdded(res, lastIdx);            //trans.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
	}
	
	void resourceAdded(RooResource res, int index) {
		for (IRooModelListener l : listeners) l.resourceAdded(this, res, index);
	}
	
	void resourceRemoved(IRooResource res, int index) {
		for (IRooModelListener l : listeners) l.resourceRemoved(this, res, index);
	}
	
	void resourceChanged(IRooResource res) {
		for (IRooModelListener l : listeners) l.resourceChanged(this, res, resources.indexOf(res));
	}
	
	@Override
	public RooResource getResource(String identifier) {
		return idToResources.get(identifier);
	}
	
	@Override
	public IRooResource getResourceAt(int index) {
		return resources.get(index);
	}
	
	@Override
	public synchronized void remove(Object resource, ILog log) {
		if (!(resource instanceof RooResource)) throw new IllegalArgumentException("Can only remove resources of type RooResource.");
		RooResource res = (RooResource)resource;
		res.setParent(null);
		res.cleanupOnRemove(log);
		int idx = resources.indexOf(res);
		resources.remove(idx);
		idToResources.remove(res.getIdentifier());
		resourceRemoved(res, idx);
		res.resourceRemoved();
	}

	List<IRooModelListener> listeners = new LinkedList<IRooModelListener>();
	
	@Override
	public void addListener(IRooModelListener l) {
		listeners.add(l);
	}
	
	@Override
	public void removeListener(IRooModelListener l) {
		listeners.remove(l);
	}

	@Override
	public int getNumResources() {
		return resources.size();
	}
	
	@Override
	public List<RooResource> getResourcesList() {
		return Collections.unmodifiableList(resources);
	}

	@Override
	public void saveState(Element rootNode, Document f, ILog log) throws ModelIOException {
		for (RooResource res: resources) {
			Element resElem = f.createElement("Resource");
			res.saveState(resElem, f, log);
			rootNode.appendChild(resElem);
		}
	}

	@Override
	public int getResourceIndex(Object res) {
		return resources.indexOf(res);
	}

	public RooUploadResource addNewUploadResource(File f) {
		RooUploadResource result = new RooUploadResource(f);
		addResource(result);
		return result;
	}
	
	public RooDownloadResource addNewDownloadResource() {
		RooDownloadResource result = new RooDownloadResource();
		addResource(result);
		return result;
	}
	
}
