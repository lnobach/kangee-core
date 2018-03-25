package de.roo.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import de.roo.logging.ILog;
import de.roo.util.stream.StreamCopy;

/**
 * 
 * @author Leo Nobach
 *
 */
public class FileUtils {

	public static String getFileExtension(File f) {
		if (f.isDirectory()) return null;
		return getFileExtension(f.getName());
	}
	
	public static String getFileExtension(String name) {
		int pos = name.lastIndexOf(".");
		if (pos == -1) return null;
		return name.substring(pos+1);
	}
	
	public static String getExtensionLessFileName(File f) {
		if (f.isDirectory()) return null;
		return getExtensionLessFileName(f.getName());
	}
	
	public static String getExtensionLessFileName(String name) {
		int pos = name.lastIndexOf(".");
		if (pos == -1) return null;
		return name.substring(0, pos);
	}
	
	
	public static String shortenFileName(String fileName, int maxChars) {
		final String TAIL_SEQ = "...";
		
		if (fileName.length() < maxChars) return fileName;
		
		String fNameExt = FileUtils.getFileExtension(fileName);
		if (fNameExt == null) {
			//File name has no extension part
			int noExtLength = maxChars - TAIL_SEQ.length();
			return fileName.substring(0, noExtLength) + TAIL_SEQ;
		}
		String fNameNoExt = FileUtils.getExtensionLessFileName(fileName);
		int noExtLength = maxChars - TAIL_SEQ.length() - fNameExt.length() -1;
		if (noExtLength < 0) noExtLength = 0;
		
		return fNameNoExt.substring(0, noExtLength) + TAIL_SEQ + " ." + fNameExt;
		
	}
	
	public static String replaceIllegalFileNameChars(String fileName) {
		String result = fileName.replaceAll("[/\\\\?\\*:\\?\"<>|]", "_"); //Replacing some illegal chars.
		result = result.replaceAll("\\.\\.", "_"); //Replacing double dots (parent dir).
		return result;
	}
	
	public static boolean moveFile(File source, File target, ILog log) {
		log.dbg(FileUtils.class, "Trying to move file from " + source + " to " + target);
		if (!source.renameTo(target)) {
			log.dbg(FileUtils.class, "Could not move file " + source + " to " + target 
					+ ". Trying to copy it (takes longer) and delete the source afterwards.");
			if (copyFile(source, target, log)) {
				if (!source.delete()) log.warn(FileUtils.class, "Could not delete source file," + source + " ignoring.");
				return true;
			} else return false;
		}
		return true;
	}

	private static boolean copyFile(File source, File target, ILog log) {
		try {
			FileInputStream fis = new FileInputStream(source);
			BufferedInputStream bufIS = new BufferedInputStream(fis);
			FileOutputStream fos = new FileOutputStream(target);
			BufferedOutputStream bufOS = new BufferedOutputStream(fos);
			StreamCopy cp = new StreamCopy();
			
			cp.copy(bufIS, bufOS);
			
			bufOS.flush();
			bufIS.close();
			bufOS.close();
			fis.close();
			fos.close();
			return true;
		} catch (IOException ex) {
			log.error(FileUtils.class, "Could not copy file " + source + " to " + target, ex);
			return false;
		}
	}
	
}












