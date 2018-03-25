package de.roo.model;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import de.roo.logging.ILog;
import de.roo.model.uiview.IRooUploadResource;
import de.roo.util.FileUtils;
import de.roo.util.HashToolkit;
import de.roo.util.server.MIMETypes;

/**
 * 
 * @author Leo Nobach
 *
 */
public class RooUploadResource extends RooResource implements IRooUploadResource {

		/**
		 * 
		 */
		private static final long serialVersionUID = 3270759358972483279L;
		
		transient Map<String, byte[]> calculatedHashes = null;
		
		public RooUploadResource(File file) {
			this.setFile(file);
			this.setMIMEType(MIMETypes.mimeTypeFromExt(FileUtils.getFileExtension(getPlainFileName())));
		}
		
		public RooUploadResource(Element elem, ILog log) throws ModelIOException {
			super(elem, log);
			
			String filename = elem.getAttribute("file");
			if ("".equals(filename) || filename == null) throw new ModelIOException("File attribute is empty, is required in upload resources.");
			File f = new File(filename);
			if (!f.exists()) throw new ModelIOException("File " + filename + " does not exist (anymore)");
			if (!f.isFile()) throw new ModelIOException("'File' " + filename + " is not a regular file");
			if (!f.canRead()) throw new ModelIOException("Can not read from file " + filename);
			this.setFile(f);
		}

		public String toString() {
			return "(/" + identifier + "/" + getHttpFileName() + ")";
		}

		@Override
		public String getPlainFileName() {
			synchronized (treeLock) {
				return f.getName();
			}
		}
		
		public void saveState(Element rootNode, Document fact, ILog log) throws ModelIOException {
			super.saveState(rootNode, fact, log);
			rootNode.setAttribute("type", "upload");
		}
		
		public byte[] getHash(String mdType, ILog log) throws NoSuchAlgorithmException, IOException {
			if (calculatedHashes == null) calculatedHashes = new HashMap<String, byte[]>();
			synchronized(treeLock) {
				byte[] hash = calculatedHashes.get(mdType);
				if (hash == null) hash = getNewHash(mdType, log);
				return hash;
			}
		}
		
		public byte[] getNewHash(String mdType, ILog log) throws NoSuchAlgorithmException, IOException {
			if (calculatedHashes == null) calculatedHashes = new HashMap<String, byte[]>();
			long startTime = System.currentTimeMillis();
			log.dbg(this, "Starting calculation of file hash for " + f + " of type " + mdType);
			byte[] result = HashToolkit.getHashFromFile(f, mdType);
			long duration = System.currentTimeMillis() - startTime;
			log.dbg(this, "Finished calculation of file hash for " + f + " Duration: " + duration + " ms");
			calculatedHashes.put(mdType, result);
			return result;
		}

}
