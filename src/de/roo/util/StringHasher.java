package de.roo.util;

/**
 * 
 * @author Leo Nobach
 *
 */
public class StringHasher {
	
	public static String hash(int length) {
		StringBuilder b = new StringBuilder(length);
		for (int i = 0; i < length; i++) {
			double rand = Math.random();
            double c;
            if (rand < 0.5) c = rand*52d + 'A';
            else c = (rand - 0.5)*52d + 'a';
            b.append((char)c);
		}
		return b.toString();
	}
	
}
