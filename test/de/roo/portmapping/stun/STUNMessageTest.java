package de.roo.portmapping.stun;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Test;

import junit.framework.Assert;
import de.roo.util.ByteArrayToolkit;
import de.roo.util.ByteArrayToolkit.Radix;

public class STUNMessageTest {

	static final int BUFFER_SIZE = 1024;
	
	@Test
	public void testEmptytMsg() {
		List<STUNAttribute> attrs = Collections.emptyList();
		STUNMessage msg = new STUNMessage(STUNMessage.MSG_BINDING_REQUEST, attrs);
		testDeSerializeEquals(msg);
	}
	
	@Test
	public void testOneAttribute() {
		List<STUNAttribute> attrs = new ArrayList<STUNAttribute>(1);
		
		attrs.add(new STUNAttribute(STUNAttribute.ATTR_CHANGE_REQUEST, new byte[]{1,2,3,4,5}));
		
		STUNMessage msg = new STUNMessage(STUNMessage.MSG_BINDING_REQUEST, attrs);
		testDeSerializeEquals(msg);
	}
	
	@Test
	public void testManyAttributes() {
		List<STUNAttribute> attrs = new ArrayList<STUNAttribute>(1);
		
		attrs.add(new STUNAttribute(STUNAttribute.ATTR_CHANGE_REQUEST, new byte[]{1,2,3,4,5}));
		attrs.add(new STUNAttribute(STUNAttribute.ATTR_CHANGED_ADDRESS, new byte[]{}));
		attrs.add(new STUNAttribute(STUNAttribute.ATTR_ERROR_CODE, new byte[]{1,2,3}));
		attrs.add(new STUNAttribute(STUNAttribute.ATTR_MAPPED_ADDRESS, new byte[]{1,2,3,4,5,6,7,8}));
		
		STUNMessage msg = new STUNMessage(STUNMessage.MSG_BINDING_REQUEST, attrs);
		testDeSerializeEquals(msg);
	}
	
	public void testDeSerializeEquals(STUNMessage msg) {
		byte[] buffer = new byte[1024];
		System.out.println("Message is: " + msg);
		STUNTransactionID id = STUNTransactionID.newRandomTransactionID();
		int len = msg.writeBytes(buffer, 0, id);
		//System.out.println(ByteArrayToolkit.printArrayRFCLike(buffer, 0, len, Radix.Hexadecimal));
		System.out.println(ByteArrayToolkit.printArrayRFCLike(buffer, 0, len, Radix.Decimal));
		System.out.println(ByteArrayToolkit.printArrayRFCLike(buffer, 0, len, Radix.Binary));
		STUNMessage resMsg = STUNMessage.fromBytes(buffer, 0, len, id);
		Assert.assertEquals(msg, resMsg);
	}
	
}
