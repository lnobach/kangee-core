package de.roo.util;

import java.math.BigInteger;

/**
 * 
 * @author Leo Nobach
 *
 */
public class ByteArrayToolkit {

	/**
	 * Returns the integer contained in 4 bytes of b starting with the offset.
	 * @param b
	 * @param offset
	 * @return
	 */
	public static int extractInteger(byte[] b, int offset) {
		return b[offset]<<24 | (b[offset+1]&0xff)<<16 | (b[offset+2]&0xff)<<8 | (b[offset+3]&0xff);
	}
	
	/**
	 * Overwrites 4 succeeding bytes in the array b with the given integer, starting with the byte
	 * located at offset.
	 * 
	 * @param integer
	 * @param offset
	 * @return
	 */
	public static void insertInteger(int integer, byte[] b, int offset) {
		b[offset] = (byte)(integer>>24);
		b[offset+1] = (byte)(integer>>16);
		b[offset+2] = (byte)(integer>>8);
		b[offset+3] = (byte)integer;
	}
	
	/**
	 * Returns the short contained in 2 bytes of b starting with the offset.
	 * @param b
	 * @param offset
	 * @return
	 */
	public static short extractShort(byte[] b, int offset) {
		return (short) (b[offset]<<8 | (b[offset+1]&0xff));
	}
	
	/**
	 * Overwrites 2 succeeding bytes in the array b with the given short, starting with the byte
	 * located at offset.
	 * 
	 * @param integer
	 * @param offset
	 * @return
	 */
	public static void insertShort(short shrt, byte[] b, int offset) {
		b[offset] = (byte)(shrt>>8);
		b[offset+1] = (byte)shrt;
	}
	
	/**
	 * Compares two arrays from the given offset for length bytes.
	 * @param a1
	 * @param a1offset
	 * @param a2
	 * @param a2offset
	 * @param length
	 * @return
	 */
	public static boolean equals(byte[] a1, int a1offset, byte[] a2, int a2offset, int length) {
		
		if (a1offset + length > a1.length) throw new ArrayIndexOutOfBoundsException("a1 is smaller(" + a1.length
				+ ") than selected offset ("+ a1offset + ") plus length (" + length + ")");
		if (a2offset + length > a2.length) throw new ArrayIndexOutOfBoundsException("a2 is smaller(" + a2.length
				+ ") than selected offset ("+ a2offset + ") plus length (" + length + ")");
		
		for (int i = 0; i < length; i++) {
			if (a1[a1offset+i] != a2[a2offset+i]) return false;
		}
		return true;
	}
	
	public static String printArray(byte[] arr, int offset, int length, int radix) {
		byte[] prArr = new byte[length];
		System.arraycopy(arr, offset, prArr, 0, length);
		return new BigInteger(prArr).toString(radix);
	}
	
	public static String printArrayRFCLike(byte[] arr, int offset, int length, Radix radix) {
		StringBuffer buf = new StringBuffer();
		buf.append("=== Length: " + length + ", Radix: " + radix.toString() + "\n");
		for(int i = 0; i < length; i++) {
			String val;
			if (radix == Radix.Binary) {
				val = (Integer.toBinaryString(arr[offset+i] & 0xFF));
				for (int i2 = val.length(); i2 < 8; i2++) buf.append('0');
				buf.append(val);
			} else if (radix == Radix.Hexadecimal) {
				val = (Integer.toHexString(arr[offset+i] & 0xFF));
				if (val.length() < 2) buf.append('0');
				buf.append(val);
			} else {
				val = (String.valueOf(arr[offset+i] & 0xFF));
				for (int i2 = val.length(); i2 < 3; i2++) buf.append('0');
				buf.append(val);
			}
			if (i % 4 == 3) buf.append('\n');
			else buf.append(' ');
		}
		buf.append("\n===");
		return buf.toString();
	}
	
	public enum Radix {
		Binary,
		Decimal,
		Hexadecimal;
	}
	
}





