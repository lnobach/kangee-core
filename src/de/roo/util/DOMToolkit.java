package de.roo.util;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * 
 * @author Leo Nobach
 *
 */
public class DOMToolkit {

	public static Element getFirstChildElemMatching(Node parent, String tagName, boolean useLocalNames) {
		NodeList l = parent.getChildNodes();
		
		for (int i=0; i<l.getLength(); i++) {
			Node n = l.item(i);
			if (n.getNodeType() == Node.ELEMENT_NODE && tagName.equals(useLocalNames?n.getLocalName():n.getNodeName())) return (Element)n;
		}
		return null;
	}
	
	public static List<Element> getAllChildElemsMatching(Node parent, String tagName, boolean useLocalNames) {
		NodeList l = parent.getChildNodes();
		List<Element> result = new ArrayList<Element>(30);
		for (int i=0; i<l.getLength(); i++) {
			Node n = l.item(i);
			if (n.getNodeType() == Node.ELEMENT_NODE && tagName.equals(useLocalNames?n.getLocalName():n.getNodeName()))
				result.add((Element)n);
		}
		return result;
	}
	
	public static String getFirstChildElemMatchingTextContent(Node parent, String tagName, boolean useLocalNames) {
		Element e = getFirstChildElemMatching(parent, tagName, useLocalNames);
		if (e == null) return null;
		return e.getTextContent();
	}
	
}
