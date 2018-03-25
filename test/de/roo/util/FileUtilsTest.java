package de.roo.util;

/**
 * 
 * @author Leo Nobach (dev@getkangee.com)
 *
 */
public class FileUtilsTest {

	public static void main(String[] args) {
		
		System.out.println(FileUtils.replaceIllegalFileNameChars("../..BreakOut/Hackversuch.txt"));
		System.out.println(FileUtils.replaceIllegalFileNameChars("Test.txt"));
		System.out.println(FileUtils.replaceIllegalFileNameChars("Testäöüß"));
		System.out.println(FileUtils.replaceIllegalFileNameChars("/\\:*?\"<>|"));
		
	}
	
}
