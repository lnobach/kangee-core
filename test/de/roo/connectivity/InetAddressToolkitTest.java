package de.roo.connectivity;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Comparator;

import junit.framework.Assert;

import org.junit.Test;

import de.roo.util.InetAddressToolkit;
import de.roo.util.InetAddressToolkit.InetAddrRange;

/**
 * 
 * @author Leo Nobach (dev@getkangee.com)
 *
 */
public class InetAddressToolkitTest {

	@Test
	public void testComparator() throws UnknownHostException {
		Comparator<InetAddress> c = InetAddressToolkit.INET_ADDR_COMP;
		
		Assert.assertTrue(c.compare(InetAddress.getByName("192.168.2.1"), InetAddress.getByName("192.168.2.2")) < 0);
		Assert.assertTrue(c.compare(InetAddress.getByName("192.168.2.1"), InetAddress.getByName("192.168.252.1")) < 0);
		Assert.assertTrue(c.compare(InetAddress.getByName("192.168.0.0"), InetAddress.getByName("192.168.0.1")) < 0);
		Assert.assertTrue(c.compare(InetAddress.getByName("192.168.251.1"), InetAddress.getByName("192.168.252.1")) < 0);
		
		Assert.assertTrue(c.compare(InetAddress.getByName("192.168.252.1"), InetAddress.getByName("192.168.252.1")) == 0);
		
		Assert.assertTrue(c.compare(InetAddress.getByName("255.255.255.255"), InetAddress.getByName("0.0.0.0")) > 0);
		Assert.assertTrue(c.compare(InetAddress.getByName("192.168.0.0"), InetAddress.getByName("192.167.255.255")) > 0);
		Assert.assertTrue(c.compare(InetAddress.getByName("192.168.252.1"), InetAddress.getByName("192.168.251.2")) > 0);
	}
	
	@Test
	public void testRanges1() throws UnknownHostException {
		
		InetAddrRange range = new InetAddrRange(InetAddress.getByName("192.168.0.0"), InetAddress.getByName("192.168.255.255"));
		Assert.assertTrue(range.contains(InetAddress.getByName("192.168.2.1")));
		Assert.assertTrue(range.contains(InetAddress.getByName("192.168.252.1")));
		Assert.assertFalse(range.contains(InetAddress.getByName("192.167.252.1")));
		Assert.assertFalse(range.contains(InetAddress.getByName("191.167.252.1")));
	}
	
	@Test
	public void testRanges2() throws UnknownHostException {
		
		InetAddrRange range = new InetAddrRange(InetAddress.getByName("192.168.252.1"), InetAddress.getByName("192.168.253.15"));
		Assert.assertTrue(range.contains(InetAddress.getByName("192.168.252.1")));
		Assert.assertTrue(range.contains(InetAddress.getByName("192.168.252.2")));
		Assert.assertTrue(range.contains(InetAddress.getByName("192.168.253.2")));
		Assert.assertTrue(range.contains(InetAddress.getByName("192.168.253.15")));
		Assert.assertFalse(range.contains(InetAddress.getByName("191.167.253.15")));
		Assert.assertFalse(range.contains(InetAddress.getByName("0.0.0.0")));
		Assert.assertFalse(range.contains(InetAddress.getByName("255.255.255.255")));
	}
	
}
