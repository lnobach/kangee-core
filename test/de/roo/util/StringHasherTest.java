package de.roo.util;

/**
 * 
 * @author Leo Nobach (dev@getkangee.com)
 *
 */
public class StringHasherTest {

	public static void main(String[] args) {
		for(int i = 0; i < 100; i++)
			System.out.println(StringHasher.hash(32));
		
	}
	
}
