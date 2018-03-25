package de.roo.model;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import de.roo.logging.ILog;

/**
 * 
 * @author Leo Nobach
 *
 */
public interface IXMLSavable {

	public void saveState(Element rootNode, Document f, ILog log) throws ModelIOException;
	
}
