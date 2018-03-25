package de.roo.srv;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.roo.BuildConstants;
import de.roo.logging.ILog;
import de.roo.srvApi.IRequest;
import de.roo.srvApi.ResponseHeaders;
import de.roo.util.stream.LimitedEOFStream;

/**
 * Support for Partial Content, Download Resuming etc.
 * @author Leo Nobach
 *
 */
public class RangeHandler {

	static Pattern rPat = Pattern.compile("^([a-zA-Z]*)=(\\d*)-(\\d*)$");
	
	long begin = 0;
	long end = -1;
	
	long fLength;
	
	boolean rangeUsed = false;

	private File f;
	
	public RangeHandler(IRequest req, ILog log, File f) {
		this.f = f;
		this.fLength = f.length();
		String hdr = req.getHeader("Range");
		
		if (hdr != null) {
			Matcher m = rPat.matcher(hdr);
			if (m.find()) {
				String type = m.group(1);
				String beginStr = m.group(2);
				String endStr = m.group(3);
				
				if (type.equalsIgnoreCase("bytes")) {
					beginStr=beginStr.trim();
					if (beginStr != null && !"".equals(beginStr)) {
						long beginInt = Long.parseLong(beginStr);
						if (beginInt >= 0 && beginInt < fLength) {
							begin = beginInt;
							rangeUsed = true;
						} else {
							log.warn(this, "Range begin is smaller than 0 or greater or equal the"
									+ " file length " + fLength + ": " + beginInt);
						}
					}
					endStr = endStr.trim();
					if (rangeUsed) {
						if (endStr != null && !"".equals(endStr)) {
							long endInt = Long.parseLong(endStr);
							if (endInt >= 0 && endInt < fLength) {
								end = endInt;
							} else {
								log.warn(this, "Range end is smaller than 0 or greater or equal the"
										+ " file length " + fLength + ": " + endInt);
								rangeUsed = false;
							}
						} else {
							end = fLength-1;
						}
						if (end >= 0 && end <= begin) {
							log.warn(this, "End(" + end + ") is equal to or smaller than begin (" + begin + "("
									+ BuildConstants.PROD_TINY_NAME + "will transfer the whole resource).");
							rangeUsed = false;
						}
					}
				} else {
					log.warn(this, BuildConstants.PROD_TINY_NAME
							+ " does not support the range unit " + type);
				}
				
				log.dbg(this, "Sending partial content from " + begin + " to " + end);
				
			} else {
				log.warn(this, "The Range header '" + hdr
						+ "' that the client sent is malformed. Sending complete file.");
			}
		}
		
	}
	
	public void createRangeRespHdrs(ResponseHeaders respHdrs) {
		long len;
		if (rangeUsed) {
			respHdrs.addHeader("Content-Range", "bytes " +((begin > 0)?begin:"")
					+ "-" + ((end > 0)?end:"") + "/" + fLength);
			len = end-begin+1;
		} else {
			len = fLength;
		}	
			
		respHdrs.addHeader("Content-Length", String.valueOf(len));
	}
	
	boolean isRangeUsed() {
		return rangeUsed;
	}
	
	public InputStream getRangedStream() throws IOException {
		FileInputStream is = new FileInputStream(f);
		if (!rangeUsed) return is;
		if (begin > 0) is.skip(begin);
		if (end > 0) 
			return new LimitedEOFStream(is, end-begin+1);
		else
			return is;
	}
	
	/*
	public static void main(String[] args) {
		
		testMatcher("abcde");
		testMatcher("bytes=-145");
		testMatcher("bytes=245-886");
		testMatcher("bytes=455-");
		
	}
	
	public static void testMatcher(String s) {
		Matcher m = rPat.matcher(s);
		
		while (m.find()) {
			String type = m.group(1);
			String begin = m.group(2);
			String end = m.group(3);
			
			System.out.println(s + " Found:'" + type + "', '" + begin + "', '" + end + "'");
		}
	}http://79.218.79.54:10435/sBBZpxqEkn/Fyrion+-+Circle+Of+Lies.mp3
	*/
	
	public long getBegin() {
		return begin;
	}
	
	public long getEnd() {
		return end;
	}
	
	public long getFileLength() {
		return fLength;
	}
	
	public long getPartialContentLength() {
		if (!rangeUsed) return fLength;
		return end-begin+1;
	}
	
}
