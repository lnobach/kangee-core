package de.roo.portmapping.upnp;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import de.roo.portmapping.PortMappingException;

/**
 * 
 * @author Leo Nobach
 *
 */
public class SOAPErrorException extends PortMappingException {

	String faultCode;
	String faultStr;
	int errorCode = -1;
	String errorDescr;
	
	/**
	 * Handles the most important error messages.
	 * @param e
	 * @throws PortMappingException
	 */
	public static void throwExceptionFromSOAPError(SOAPErrorException e) throws PortMappingException {
		if (e.getErrorCode() == 713 || "SpecifiedArrayIndexInvalid".equalsIgnoreCase(e.getErrorDescr())) {
			throw new ArrayIndexOutOfBoundsException();
		} else if (e.getErrorCode() == 403 || "Not available Action".equalsIgnoreCase(e.getErrorDescr())) {
			throw new PortMappingException.PortMappingNotAllowed();
		} else if (e.getErrorCode() == 718 || "ConflictInMappingEntry".equalsIgnoreCase(e.getErrorDescr())) {
			throw new PortMappingException.PortMappingConflict(e);
		} else throw e;
		
	}
	
	public SOAPErrorException(String faultCode, String faultStr,
			Node faultDetail) {
		super();
		this.faultCode = faultCode;
		this.faultStr = faultStr;
		
		NodeList faultChildren = faultDetail.getChildNodes();
		for (int i = 0; i < faultChildren.getLength(); i++) {
			Node faultChild = faultChildren.item(i);
			if ("UPnPError".equals(faultChild
					.getLocalName())) {
				NodeList upErrChildren = faultChild.getChildNodes();
				for (int i2 = 0; i2 < upErrChildren.getLength(); i2++) {
					Node upErrChild = upErrChildren.item(i2);
					if ("errorCode".equals(upErrChild
							.getLocalName())) {
						try {
							this.errorCode = Integer.parseInt(upErrChild.getTextContent());
						} catch (NumberFormatException e) {
							//Nothing to do
						}
					} else if ("errorDescription".equals(upErrChild
							.getLocalName())) {
						this.errorDescr = upErrChild.getTextContent();
					}
				}
			}
		}
	}
	
	public String getFaultCode() {
		return faultCode;
	}
	
	public int getErrorCode() {
		return errorCode;
	}
	
	public String getFaultStr() {
		return faultStr;
	}
	
	public String getErrorDescr() {
		return errorDescr;
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = -8599159328442017107L;
	
	public String getMessage() {
		return "faultStr=" + faultStr + ", faultCode=" + faultCode + ", errorCode=" + errorCode + ", errorDescr=" + errorDescr;
	}

	
}
