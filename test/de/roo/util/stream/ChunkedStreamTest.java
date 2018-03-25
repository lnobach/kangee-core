package de.roo.util.stream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import junit.framework.Assert;

import org.junit.Test;

import de.roo.httpcli.ChunkHandlingStream;

/**
 * 
 * @author Leo Nobach (dev@getkangee.com)
 *
 */
public class ChunkedStreamTest {

	@Test
	public void test1() throws Exception {
		testStringed("5\r\nABCDE\r\n14\r\nFGHIJKLMNOPQRSTUVWXY\r\n32\r\nZABCDEFGHIJKLMNOPQRSTUVWXYZ" + 
				"abcdefghijklmnopqrstuvw\r\n1D\r\nxyzabcdefghijklmnopqrstuvwxyz\r\n0\r\n\r\n", 
				"ABCDEFGHIJKLMNOPQRSTUVWXYZABCDEFGHIJKLMNOPQRSTUVWXYZ" + 
				"abcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyz");
	}
	
	@Test
	public void test2() throws Exception {
		testStringed("2\r\nAB\r\n2\r\nCD\r\n5\r\nEFGGG\r\n2\r\nGG\r\n0\r\n\r\n", 
				"ABCDEFGGGGG");
	}
	
	@Test
	public void testEmpty() throws Exception {
		testStringed("0\r\n\r\n", 
				"");
	}
	
	@Test
	public void testErroneous() throws Exception {
		testStringed("AA3\r\nABC\r\n", 
				"ABC\r\n");
	}
	
	void testStringed(String input, String expectedOutput) throws IOException {
		testStringed(input, expectedOutput, 1);
		testStringed(input, expectedOutput, 2);
		testStringed(input, expectedOutput, 10);
		testStringed(input, expectedOutput, 17);
		testStringed(input, expectedOutput, 33);
		testStringed(input, expectedOutput, 100);
		testStringed(input, expectedOutput, 500);
		testStringed(input, expectedOutput, 2048);
	}
	
	void testStringed(String input, String expectedOutput, int bufferSize) throws IOException {
		ByteArrayInputStream is = new ByteArrayInputStream(input.getBytes());
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		new StreamCopy(bufferSize).copy(new ChunkHandlingStream(is), os);
		Assert.assertEquals(expectedOutput, os.toString());
	}
	
}
