package de.roo.engine;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import de.roo.BuildConstants;
import de.roo.logging.ILog;
import de.roo.util.DOMToolkit;
import de.roo.util.VersionInfo;

public class UpdateChecker {

	private String updateCheckURL;
	private String updateDownloadURL;
	static final int READ_TIMEOUT = 2000;
	static final int CONN_TIMEOUT = 2000;
	
	static final VersionInfo FAKE_OLD_VERSION = new VersionInfo(0, 0);
	static final boolean TEST_OLD_VERSION_FAKE = false;

	public UpdateChecker(String updateCheckURL, String updateDownloadURL) {
		this.updateCheckURL = updateCheckURL;
		this.updateDownloadURL = updateDownloadURL;
	}
	
	public UpdateInfo check(ILog log) {
		try {
			return checkStrict(log);
		} catch (UpdateCheckException e) {
			log.warn(this, "Could not check for updates.", e);
			return null;
		}
	}
	
	public UpdateInfo checkStrict(ILog log) throws UpdateCheckException {
		try {
			URL url = new URL(updateCheckURL);
			URL updateDownloadUrl = new URL(updateDownloadURL);
			Document doc = getFromUpdateServer(url);
			VersionInfo newestVer = versionFromDOM(doc);
			VersionInfo localVer = TEST_OLD_VERSION_FAKE?FAKE_OLD_VERSION:BuildConstants.getLocalProdVersion();
			log.dbg(this, "Newest version provided via " + updateDownloadUrl.toExternalForm() + ": " + newestVer + ". You have version " + localVer + ".");
			return new UpdateInfo(newestVer, localVer, updateDownloadUrl);
		} catch (MalformedURLException e) {
			throw new UpdateCheckException(e);
		}
	}

	private Document getFromUpdateServer(URL url) throws UpdateCheckException {
		URLConnection conn;
		try {
			conn = url.openConnection();
			conn.setConnectTimeout(CONN_TIMEOUT);
			conn.setReadTimeout(READ_TIMEOUT);
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document doc = db.parse(conn.getInputStream());
			doc.getDocumentElement().normalize();
			return doc;
		} catch (IOException e) {
			throw new UpdateCheckException("I/O problems while requesting whether " + BuildConstants.PROD_TINY_NAME + " is the newest version ", e);
		} catch (SAXException e) {
			throw new UpdateCheckException("Problems parsing response of update server.", e);
		} catch (ParserConfigurationException e) {
			throw new UpdateCheckException("Problems configuring parser for response of update server.", e);
		}
	}
	
	private VersionInfo versionFromDOM(Document doc) throws UpdateCheckException {
		Element versioninfo = DOMToolkit.getFirstChildElemMatching(doc, "versioninfo", false);
		if (versioninfo == null) throw new UpdateCheckException("The root element <updateinfo> was not found in the XML update dataset received from server.");
		Element majorRevElem = DOMToolkit.getFirstChildElemMatching(versioninfo, "curProdMajorRev", false);
		if (majorRevElem == null) throw new UpdateCheckException("The major revision element <curProdMajorRev> was not found in the XML update dataset received from server.");
		Element minorRevElem = DOMToolkit.getFirstChildElemMatching(versioninfo, "curProdMinorRev", false);
		if (minorRevElem == null) throw new UpdateCheckException("The minor revision element <curProdMinorRev> was not found in the XML update dataset received from server.");
		
		int majorRevInt;
		try {
			majorRevInt = Integer.parseInt(majorRevElem.getTextContent());
		} catch (NumberFormatException e) {
			throw new UpdateCheckException("The major revision '" + majorRevElem.getTextContent() + "' could not be parsed as an integer.", e);
		}
		
		int minorRevInt;
		try {
			minorRevInt = Integer.parseInt(minorRevElem.getTextContent());
		} catch (NumberFormatException e) {
			throw new UpdateCheckException("The minor revision '" + minorRevElem.getTextContent() + "' could not be parsed as an integer.", e);
		}
		return new VersionInfo(majorRevInt, minorRevInt);
	}	
	
	public static class UpdateInfo {
		
		private VersionInfo newestVer;
		private VersionInfo localVer;
		private URL updateDownloadURL;

		public UpdateInfo(VersionInfo newestVer, VersionInfo localVer, URL updateDownloadURL) {
			this.newestVer = newestVer;
			this.localVer = localVer;
			this.updateDownloadURL = updateDownloadURL;
		}

		public URL getUpdateDownloadURL() {
			return updateDownloadURL;
		}
		
		public VersionInfo getNewestVersion() {
			return newestVer;
		}

		public VersionInfo getLocalVersion() {
			return localVer;
		}
		
		public int getVersionComparison() {
			return newestVer.compareTo(localVer);
		}
		
		public boolean shouldUpdate() {
			return getVersionComparison() > 0;
		}

		public String toString() {
			return "Newest: " + newestVer + ", Local: " + localVer + ", verComp: " + getVersionComparison() + ", shouldUpdate: " + shouldUpdate();
		}
		
	}
	
}






