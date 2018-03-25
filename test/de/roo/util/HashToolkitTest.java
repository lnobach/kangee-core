package de.roo.util;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;

/**
 * 
 * @author Leo Nobach (dev@getkangee.com)
 *
 */
public class HashToolkitTest {

	

	public static void main(String[] args) {
		File f = new File("/home/leo/uebung_2.pdf");
		try {
			printHash(f, "MD5");
			printHash(f, "SHA1");
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public static void printHash(File f, String hashType) throws NoSuchAlgorithmException, IOException {
		System.out.println(hashType + ": " + HashToolkit.getHashFromFileHexStr(f, hashType, true));
	}
	
}
