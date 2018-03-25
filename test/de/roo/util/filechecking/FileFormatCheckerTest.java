package de.roo.util.filechecking;

import java.io.File;
import java.io.IOException;

/**
 * 
 * @author Leo Nobach (dev@getkangee.com)
 *
 */
public class FileFormatCheckerTest {

	public static void main(String[] args) {
		try {
			test1();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//test2();
	}

	private static void test1() throws IOException {
		File testDir = new File("/home/leo");
		FileFormatChecker c = new FileFormatChecker();
		
		File[] files  = testDir.listFiles();
		for (File f : files) {
			if (f.isFile()) {
				String mimeType = c.getMIME(f);
				System.out.println(f.getName() + ":   " + mimeType);
			}
		}
	}
	
}
