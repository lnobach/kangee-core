package de.roo.util.filechecking;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * 
 * @author Leo Nobach
 *
 */
public class FileFormatChecker {
	
	static final ByteRadixTreeMap<String> map = new ByteRadixTreeMap<String>();
	
	static {
		map.addPrefix(new byte[]{71, 73, 70, 56, 57, 97}, "image/gif");			//0x47, 0x49, 0x46, 0x38, 0x39, 0x61
		map.addPrefix(new byte[]{71, 73, 70, 56, 55, 97}, "image/gif");
		map.addPrefix(new byte[]{-119, 80, 78, 71, 13, 10, 26, 10}, "image/png");
		map.addPrefix(new byte[]{37, 80, 68, 70}, "application/pdf"); //"%PDF" (25 50 44 46).
		//map.addPrefix(u(new int[]{0x25, 0x50, 0x44, 0x46}), "application/pdf");
	}
	
	/**
	 * Returns the MIME type of the file, or null if the file cannot
	 * be read or is of an unknown format.
	 */
	public String getMIME(File f) throws IOException {
		FileInputStream fis = new FileInputStream(f);
		return getMIME(fis);
	}
	
	public String getMIME(InputStream is) throws IOException {
		byte[] prefix = getFirstBytes(is, map.getMaximumPrefixLength());
		return map.getResult(prefix);
	}

	/**
	 * Returns null if the file cannot be opened
	 * @param f
	 * @param count
	 * @return
	 */
	protected byte[] getFirstBytes(InputStream is, int count) throws IOException {
		byte[] bytes = new byte[count];
		is.read(bytes);
		is.close();
		return bytes;

	}

	/**
	 * Converts to signed byte array and prints it.
	 * @param b
	 * @return
	 */
	public static byte[] u(int[] b) {
		
		byte[] res = new byte[b.length];
		System.out.print("byte[]{");
		boolean first = true;
		for (int i = 0; i<b.length; i++) {
			if (!first) System.out.print(", ");
			res[i] = u(b[i]);
			System.out.print(res[i]);
			first = false;
		}
		System.out.println("}");
		return res;
	}
	
	/**
	 * Converts to signed byte
	 * @param i
	 * @return
	 */
	private static byte u(int i) {
		return (byte)i;
	}
	
	/**
	 * Converts to signed byte array and prints it.
	 * @param b
	 * @return
	 */
	public static String print(byte[] b) {
		
		StringBuilder res = new StringBuilder();
		
		res.append("byte[]{");
		boolean first = true;
		for (int i = 0; i<b.length; i++) {
			if (!first) res.append(", ");
			res.append(b[i]);
			first = false;
		}
		res.append("}");
		return res.toString();
	}
	
}











