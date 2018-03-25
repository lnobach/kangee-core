package de.roo.util.filechecking;

import junit.framework.Assert;

import org.junit.Test;

import de.roo.util.filechecking.ByteRadixTreeMap;

/**
 * 
 * @author Leo Nobach (dev@getkangee.com)
 *
 */
public class ByteRadixTreeMapTest {

	ByteRadixTreeMap<String> map;
	
	@Test
	public void testBuildTreeMap() {
		
		map = new ByteRadixTreeMap<String>();
		
		map.addPrefix(arr("abcdefg"), "Result1");
		map.addPrefix(arr("abcdefh"), "Result2");
		map.addPrefix(arr("abcdxxx"), "Result3");
		
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testBuildWrongTreeMap() {
		
		map = new ByteRadixTreeMap<String>();
		
		map.addPrefix(arr("abcdefh"), "Result2");
		map.addPrefix(arr("abcde"), "Result3");
		
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testBuildWrongTreeMap2() {
		
		map = new ByteRadixTreeMap<String>();
		
		map.addPrefix(arr("abcde"), "Result2");
		map.addPrefix(arr("abcdefgh"), "Result3");
		
	}
	
	@Test
	public void testRetrievePrefix() {
		
		map = new ByteRadixTreeMap<String>();
		
		map.addPrefix(arr("abc"), "Result1");
		map.addPrefix(arr("abdfg"), "Result2");
		map.addPrefix(arr("abdff"), "Result3");
		map.addPrefix(arr("abefgaw"), "Result4");
		map.addPrefix(arr("abefaaw"), "Result5");
		map.addPrefix(arr("aa"), "Result6");
		
		Assert.assertEquals("Result6", map.getResult(arr("aaaaaaaa")));
		Assert.assertEquals("Result1", map.getResult(arr("abcdefg")));
		Assert.assertEquals("Result3", map.getResult(arr("abdff")));
		Assert.assertEquals("Result5", map.getResult(arr("abefaaw")));
		Assert.assertEquals("Result4", map.getResult(arr("abefgaw")));
		Assert.assertNull(map.getResult(arr("ab")));
		Assert.assertNull(map.getResult(arr("abd")));
		Assert.assertNull(map.getResult(arr("abdf")));
		Assert.assertNull(map.getResult(arr("arr")));
		Assert.assertNull(map.getResult(arr("hypo")));
		
	}
	
	
	
	byte[] arr(String input) {
		return input.getBytes();
	}
	
}
