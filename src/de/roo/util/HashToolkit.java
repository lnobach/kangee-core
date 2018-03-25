package de.roo.util;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * 
 * @author Leo Nobach
 *
 */
public class HashToolkit {

	static final int BUF_SIZE = 2048;

	public static String getHashFromFileHexStr(File f, String mdType,
			boolean separateWhitespace) throws NoSuchAlgorithmException,
			IOException {
		return convertToHex(getHashFromFile(f, mdType), separateWhitespace);
	}

	public static byte[] getHashFromFile(File f, String mdType)
			throws NoSuchAlgorithmException, IOException {
		MessageDigest digest = java.security.MessageDigest.getInstance(mdType);
		updateDigestFromFile(digest, f);
		return digest.digest();
	}

	public static void updateDigestFromFile(MessageDigest digest, File f)
			throws IOException {
		FileInputStream is = new FileInputStream(f);
		BufferedInputStream bufStr = new BufferedInputStream(is);
		updateDigestFromStream(digest, bufStr);
		bufStr.close();
		is.close();
	}

	public static void updateDigestFromStream(MessageDigest digest,
			InputStream is) throws IOException {
		byte[] buf = new byte[BUF_SIZE];
		int len;
		while (true) {
			len = is.read(buf);
			if (len < 0)
				break;
			digest.update(buf, 0, len);
		}
	}

	public static String convertToHex(byte[] byteVals, boolean separate) {
		StringBuffer buf = new StringBuffer();
		for (byte b : byteVals) {
			String s = Integer.toHexString(0xFF & b);
			if (s.length() == 1)
				buf.append("0" + s);
			else
				buf.append(s);
			if (separate)
				buf.append(' ');
		}
		return buf.toString();
	}

}
