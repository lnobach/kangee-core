package de.roo.httpcli;

import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * 
 * @author Leo Nobach
 *
 */
public class DOMParser implements IResponseHandler {

	Document doc = null;
	
	@Override
	public void handleResponse(HttpResponse resp) throws IOException {
		
		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			dbf.setNamespaceAware(true);
			DocumentBuilder db = dbf.newDocumentBuilder();
			doc = db.parse(resp.getInputStream());
			doc.getDocumentElement().normalize();
		} catch (SAXException e) {
			throw new IOException(e);
		} catch (ParserConfigurationException e) {
			throw new IOException(e);
		}
	}
	
	public Document getDocument() {
		return doc;
	}
	
}
