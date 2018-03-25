package de.roo.portmapping.upnp;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.List;

import de.roo.util.Tuple;

public class UPNPXMLWriter {

	Writer wr;
	
	public UPNPXMLWriter(OutputStream os, String charset) {
		try {
			wr = new OutputStreamWriter(os, charset);
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}
	
	public void writeSOAPRequest(String action, String serviceURN,
			List<Tuple<String, String>> arguments) throws IOException {
		
		write("<?xml version=\"1.0\" encoding=\"utf-8\"?>");
		write("<s:Envelope xmlns:s=\"" + AbstractUPNPDevice.SOAP_ENV_NS + "\" s:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\">");
		write("<s:Body><u:" + action + " xmlns:u=\"" + serviceURN + "\">");
		
		for (Tuple<String, String> arg : arguments) {
			write("<" + arg.getA() + ">" + arg.getB() + "</" + arg.getA() + ">");
		}
		
		write("</u:" + action + ">");
		write("</s:Body>");
		write("</s:Envelope>");
		
		flush();
	}
	
	private void flush() throws IOException {
		wr.flush();
	}

	protected void write(String str) throws IOException {
		wr.write(str);
	}
	
}
