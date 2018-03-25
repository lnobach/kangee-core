package de.roo.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * 
 * @author Leo Nobach
 *
 */
public class CollectionUtils {

	public static <T> List<T> copy(List<T> source) {
		List<T> dest = new ArrayList<T>(source.size());
		dest.addAll(source);
		return dest;
	}
	
	public static <T> T getRandomElementFrom(List<T> l, Random rand) {
		return l.get(rand.nextInt(l.size()));
	}
	
}
