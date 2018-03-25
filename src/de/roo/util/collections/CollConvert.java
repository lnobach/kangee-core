package de.roo.util.collections;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * 
 * @author Leo Nobach
 *
 */
public class CollConvert {

	/**
	 * Converts the collection source to list dest, given the specified converter.
	 * May be not thread-safe
	 * @param <Tin>
	 * @param <Tout>
	 * @param source
	 * @param dest
	 * @return
	 */
	public static <Tin, Tout> List<Tout> convertList(Collection<Tin> source, Converter<Tin, Tout> converter) {
		List<Tout> result = new ArrayList<Tout>(source.size());
		for (Tin inObj : source) result.add(converter.convert(inObj));
		return result;
	}
	
	public static interface Converter<Tin, Tout> {
		
		public Tout convert(Tin outObj);
		
	}
	
}
