package de.roo.model;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import de.roo.configuration.IConf;
import de.roo.logging.ILog;
import de.roo.util.DOMToolkit;

/**
 * 
 * @author Leo Nobach
 *
 */
public class ModelIO {
	
	
	public static void saveModel(File target, RooModel mdl, ILog log, IConf conf) throws ModelIOException {
		try {
			DocumentBuilderFactory dbfac = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder;
			docBuilder = dbfac.newDocumentBuilder();
			Document doc = docBuilder.newDocument();
			
			Element mdlElem = doc.createElement("Model");
			mdl.saveState(mdlElem, doc, log);
			doc.appendChild(mdlElem);
			
            Transformer trans = TransformerFactory.newInstance().newTransformer();
            trans.setOutputProperty(OutputKeys.INDENT, "yes");

            FileWriter wr = new FileWriter(target);
            BufferedWriter buf = new BufferedWriter(wr);
            //Writer buf = new StringWriter();
            StreamResult result = new StreamResult(buf);
            DOMSource source = new DOMSource(doc);
            
            trans.transform(source, result);
            /*
            try {
            	trans.transform(source, result);
            } catch (NullPointerException e) {
            	System.out.println("OUTPUT IS: " + buf);
            	throw new ModelIOException(e);
            }
            */
			
		} catch (ParserConfigurationException e) {
			throw new ModelIOException(e);
		} catch (TransformerConfigurationException e) {
			throw new ModelIOException(e);
		} catch (TransformerException e) {
			throw new ModelIOException(e);
		} catch (IOException e) {
			throw new ModelIOException(e);
		} 
	}
	
	public static RooModel loadModel(File source, ILog log, IConf conf) {
		try {
			File f = source;
			if (!f.exists()) {
				log.dbg(ModelIO.class, "No state file detected. Creating new model.");
				return new RooModel();
			}
			InputStream is = new FileInputStream(f);
			BufferedInputStream buf = new BufferedInputStream(is);
			DocumentBuilder b = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			Document doc = b.parse(buf);
			
			Element root = DOMToolkit.getFirstChildElemMatching(doc, "Model", false);
			if (root == null) throw new ModelIOException("State file does not contain root element 'Model'");
			
			RooModel result = new RooModel(root, log);
			log.dbg(ModelIO.class, "Successfully loaded model from file " + f.getAbsolutePath());
			return result;
			
		} catch (SAXException e) {
			return loadModelOnError(log, e);
		} catch (IOException e) {
			return loadModelOnError(log, e);
		} catch (ParserConfigurationException e) {
			return loadModelOnError(log, e);
		} catch (ModelIOException e) {
			return loadModelOnError(log, e);
		}

	}
	
	public static RooModel loadModelOnError(ILog log, Throwable e) {
		log.error(ModelIO.class, "Error while loading the model. ", e);
		return new RooModel();
	}
	
	
}
