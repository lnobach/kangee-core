package de.roo.portmapping.upnp;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import de.roo.http.ReqMethod;
import de.roo.httpcli.DOMParser;
import de.roo.httpcli.HttpClient;
import de.roo.httpcli.HttpClientException;
import de.roo.httpcli.HttpRequest;
import de.roo.logging.ILog;
import de.roo.portmapping.PortMappingException;
import de.roo.util.DOMToolkit;
import de.roo.util.Tuple;

/**
 * 
 * @author Leo Nobach
 *
 */
public abstract class AbstractUPNPDevice {

	
	private static final String CHARSET = "utf-8";

	static final boolean DEBUG_MODE = false;
	
	public static final String SOAP_ENV_NS = "http://schemas.xmlsoap.org/soap/envelope/";
	public final String CONTROL_URN = "urn:schemas-upnp-org:control-1-0";
	
	private String controlURL;
	private URL serverURL;
	
	HttpClient httpCli = null;
	
	public AbstractUPNPDevice(URL serverURL, String controlURL) {
		this.controlURL = controlURL;
		this.serverURL = serverURL;
	}
	
	/*
	 * Deprecated in the UPnP spec.
	public String queryStateVariable(String stateVarName, ILog log) throws PortMappingException {
		List<Tuple<String, String>> args = new ArrayList<Tuple<String, String>>(1);
		args.add(new Tuple<String, String>("varName", stateVarName));
		SOAPMessage msg = doRequest("QueryStateVariable", CONTROL_URN, args, log);
		
		return "Bla";
		
	}
	*/
	
	public Map<String, String> doAction(String action, List<Tuple<String, String>> arguments, ILog log) throws PortMappingException{
		Document msg = doRequest(action, getServiceURN(), arguments, log);
		return handleSOAPResponse(msg, action + "Response");
	}
	
	/**
	 * 
	 * @param action
	 * @param arguments
	 * @param log
	 * @return the response of the SOAP UPnP request.
	 * @throws PortMappingException
	 */
	public Document doRequest(String action, String serviceURN, List<Tuple<String, String>> arguments, ILog log) throws PortMappingException {
		try {

			URL url = new URL(serverURL.getProtocol(), serverURL.getHost(),
					serverURL.getPort(), controlURL);

			String prot = serverURL.getProtocol();
			
			if (!("http".equals(prot) || "https".equals(prot))) throw new IllegalArgumentException(url + ": SOAP request must be HTTP or HTTPS");
			
			if (httpCli == null) {
				httpCli = new HttpClient(serverURL.getHost(), serverURL.getPort());
			}
			
			HttpRequest req = new HttpRequest(url);
			req.addHeader("SOAPAction", "\"" + serviceURN + "#" + action + "\"");
			req.addHeader("Content-Type", "text/xml; charset=\"utf-8\"");
			req.setMethod(ReqMethod.POST);
			
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			
			UPNPXMLWriter wr = new UPNPXMLWriter(os, CHARSET);
			wr.writeSOAPRequest(action, serviceURN, arguments);
			
			//writeSOAPRequest(action, serviceURN, arguments, os);
			//os.flush();
			os.close();
			if (DEBUG_MODE) {
				log.dbg(this, "SOAP part of the request was: " + os.toString());
			}
			req.setPOSTContent(os.toByteArray());
			
			DOMParser parser = new DOMParser();
			try {
				httpCli.makeRequest(req, log, parser);
				//httpCli.makeRequest(req, log, new Dumper(log));
			} catch (HttpClientException e) {
				throw new PortMappingException(e);
			}
			Document doc = parser.getDocument();

			if (DEBUG_MODE) log.dbg(this, "SOAP part of the response was: " + getDebugDoc(doc));
			
			return doc;

		} /*catch (UnsupportedOperationException e) {
			throw new PortMappingException(e);
		} catch (MalformedURLException e) {
			throw new PortMappingException(e);
		} catch (ProtocolException e) {
			throw new PortMappingException(e);
		} catch (FactoryConfigurationError e) {
			throw new PortMappingException(e);
		} catch (XMLStreamException e) {
			throw new PortMappingException(e);
		} */catch (IOException e) {
			throw new PortMappingException(e);
		}
	}
	
	/*
	class Dumper implements IResponseHandler {

		private ILog log;

		public Dumper(ILog log) {
			this.log = log;
		}
		
		@Override
		public void handleResponse(HttpResponse resp) throws IOException {
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			new StreamCopy().copy(resp.getInputStream(), os);
			log.dbg(this, "Response was: " + os.toString());
		}
		
	}
	*/

	/*
	private void writeSOAPRequest(String action, String serviceURN,
			List<Tuple<String, String>> arguments, OutputStream str)
			throws FactoryConfigurationError, XMLStreamException, IOException {
		XMLOutputFactory f = XMLOutputFactory.newInstance();
		f.setProperty(XMLOutputFactory.IS_REPAIRING_NAMESPACES, Boolean.TRUE);
		//XMLStreamWriter wr = f.createXMLStreamWriter(str);
		OutputStreamWriter oswr = new OutputStreamWriter(str, Charset.forName(CHARSET));
		XMLStreamWriter wr = f.createXMLStreamWriter(oswr);
		wr.writeStartDocument(CHARSET, "1.0");
		wr.setPrefix("s", SOAP_ENV_NS);
		
		wr.writeStartElement(SOAP_ENV_NS, "Envelope");
		wr.writeAttribute(SOAP_ENV_NS, "encodingStyle", "http://schemas.xmlsoap.org/soap/encoding/");
		wr.writeStartElement(SOAP_ENV_NS, "Body");
		
		
		wr.writeStartElement(serviceURN, action);
		wr.setPrefix("u", serviceURN);
		
		for (Tuple<String, String> arg : arguments) {
			wr.writeStartElement(arg.getA());
			wr.writeCharacters(arg.getB());
			wr.writeEndElement();
		}
		
		wr.writeEndElement();
		wr.writeEndElement();
		wr.writeEndElement();
		
		wr.writeEndDocument();
		wr.flush();
		wr.close();
		oswr.flush();
		oswr.close();
	}
	*/
	
	protected Map<String, String> handleSOAPResponse(Document resp, String expectedRespName) throws PortMappingException {
		Element envelope = DOMToolkit.getFirstChildElemMatching(resp, "Envelope", true);
		if (envelope == null) throw new PortMappingException("The SOAP answer had no envelope.");
		Element body = DOMToolkit.getFirstChildElemMatching(envelope, "Body", true);
		if (body == null) throw new PortMappingException("The SOAP answer had no body.");
		Element fault = DOMToolkit.getFirstChildElemMatching(body, "Fault", true);
		if (fault != null) {
			String faultCode = DOMToolkit.getFirstChildElemMatchingTextContent(fault, "faultcode", true);
			String faultStr = DOMToolkit.getFirstChildElemMatchingTextContent(fault, "faultstring", true);
			Element faultDetail = DOMToolkit.getFirstChildElemMatching(fault, "detail", true);
			SOAPErrorException.throwExceptionFromSOAPError(new SOAPErrorException(faultCode, faultStr, faultDetail));
		}
		Element expRespElem = DOMToolkit.getFirstChildElemMatching(body, expectedRespName, true);
		if (expRespElem == null) throw new PortMappingException("The expected response " + expectedRespName + "was not returned.");
		NodeList children = expRespElem.getChildNodes();
		int childC = children.getLength();
		Map<String, String> response = new HashMap<String, String>(childC);
		for (int i = 0; i < childC; i++) {
			Node child = children.item(i);
			String childName = child.getLocalName();
			String childValue = child.getTextContent();
			response.put(childName, childValue);
		}
		return response;
		
	}
	

	public static void writeToFile(Document doc, File output) {
		try {

			FileWriter w = new FileWriter(output);

			TransformerFactory tFactory = TransformerFactory.newInstance();
			Transformer transformer;

			transformer = tFactory.newTransformer();

			DOMSource source = new DOMSource(doc);
			StreamResult result = new StreamResult(w);
			transformer.transform(source, result);

			w.close();

		} catch (TransformerConfigurationException e) {
			e.printStackTrace();
		} catch (TransformerException e) {
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public static String getDebugDoc(Document doc) {
		try {

			StringWriter wr = new StringWriter();
			
			TransformerFactory tFactory = TransformerFactory.newInstance();
			Transformer transformer;

			transformer = tFactory.newTransformer();

			DOMSource source = new DOMSource(doc);
			StreamResult result = new StreamResult(wr);
			transformer.transform(source, result);
			
			return wr.toString();

		} catch (TransformerConfigurationException e) {
			e.printStackTrace();
			return null;
		} catch (TransformerException e) {
			e.printStackTrace();
			return null;
		}

	}
	
	protected abstract String getServiceURN();
	
}
