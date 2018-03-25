package de.roo.util.filechecking;

import java.util.HashMap;
import java.util.Map;

/**
 * 
 * @author Leo Nobach
 *
 */
public class ByteRadixTreeMap<TValue> {

	int maximumPrefixLength = 0;
	
	ByteRadixTreeElement rootElement;
	
	public ByteRadixTreeMap() {
		rootElement = new ByteRadixTreeElement();
	}
	
	public void addPrefix(byte[] prefix, TValue result) {
		if (prefix.length <= 0) throw new IllegalArgumentException("Cannot add an empty array.");
		if (maximumPrefixLength < prefix.length) maximumPrefixLength = prefix.length;
		ByteRadixTreeElement currentElem = rootElement;
		
		boolean createdNodes = false;
		for (int i = 0; i<prefix.length; i++) {
			ByteRadixTreeElement nextElem = currentElem.branches.get(prefix[i]);
			if (nextElem == null) {
				nextElem = new ByteRadixTreeElement();
				currentElem.branches.put(prefix[i], nextElem);
				createdNodes = true;
			}
			currentElem = nextElem;
			if (currentElem.result != null) {
				byte[] oldArray = new byte[i];
				System.arraycopy(prefix, 0, oldArray, 0, i);
				throw new IllegalArgumentException("Current element " + printByteArray(prefix) + 
						" can not be added since a prefix " + printByteArray(oldArray) + " already exists.");
			}
		}
		if (createdNodes) currentElem.result = result;
		else throw new IllegalArgumentException("Current element " + printByteArray(prefix) + " hides elements.");
	}
	
	public int getMaximumPrefixLength() {
		return maximumPrefixLength;
	}
	
	public String printByteArray(byte[] array) {
		return new String(array);
	}

	public TValue getResult(byte[] prefix) {
		
		ByteRadixTreeElement currentElem = rootElement;
		for (int i = 0; i<prefix.length; i++) {
			currentElem = currentElem.branches.get(prefix[i]);
			if (currentElem == null) return null;
			if (currentElem.result != null) return currentElem.result;
		}
		return null;
	}
	
	class ByteRadixTreeElement {
		
		TValue result;
		Map<Byte, ByteRadixTreeElement> branches = new HashMap<Byte, ByteRadixTreeElement>();
		
	}
	
}
