package de.roo.engine;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;

import de.roo.logging.ILog;
import de.roo.model.uiview.IRooDownloadResource;
import de.roo.model.uiview.IRooModel;
import de.roo.model.uiview.IRooUploadResource;

/**
 * 
 * @author Leo Nobach
 *
 */
public class FileHandler {

	private IRooModel mdl;
	private ILog log;

	public FileHandler(RooEngine eng, IRooModel mdl) {
		this.mdl = mdl;
		this.log = eng.getLog();
	}
	
	
	public boolean publishFile(File f) {
		if (f.isDirectory()) {
			log.warn(this, "File" + f + " is a directory. Currently not supported, ignoring.");
			return false;
		}
		
		IRooUploadResource res = mdl.addNewUploadResource(f);
		log.dbg(this, "Adding new upload " + res);		
		return true;
	}
	
	public void createNewDownload() {
		IRooDownloadResource res = mdl.addNewDownloadResource();
		log.dbg(this, "Adding new download " + res);
	}
	
	public void publishFiles(List<File> files) {
		for (File f : files) {
			publishFile(f);
		}
	}
	
	/**
	 * Returns true if this URL can be added
	 * @param url
	 * @return
	 */
	public boolean publishFileFromURL(URL url) {
		File f = getFileFromURL(url);
		if (f != null) {
			publishFile(f);
			return true;
		}
		return false;
	}
	
	File getFileFromURL(URL url)  {
		if (url.getProtocol().equals("file")) {
			try {
				String fileName = url.getFile();
				fileName = URLDecoder.decode(fileName, "UTF-8");
				File f = new File(fileName);
				if (!f.exists()) {
					log.error(this, "File " + f + " which should be added does not exist");
					return null;
				}
				return f;
			} catch (UnsupportedEncodingException e) {
				log.error(this, "Problems decoding file name in URL.", e);
			}
		}
		log.error(this, "URL " + url + " is not a file URL. Not supported.");
		return null;
	}
	
	/**
	 * Returns true if all URLs could be added.
	 * @param urls
	 */
	public boolean publishFilesFromURLs(List<URL> urls) {
		boolean result = true;
		List<File> files = new ArrayList<File>(urls.size());
		
		for (URL url : urls) {
			File f = getFileFromURL(url);
			if (f != null) files.add(f);
			else result = false;
		}
		publishFiles(files);
		
		return result;
		
	}
	
}
