package de.roo.portmapping.stun;

import java.lang.reflect.Field;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import de.roo.portmapping.AddressDiscoveryException;
import de.roo.util.ByteArrayToolkit;

/**
 * 
 * @author Leo Nobach
 *
 */
public class STUNMessage {

	public static final short MSG_BINDING_REQUEST = 0x0001;  // Binding Request
	public static final short MSG_BINDING_RESPONSE = 0x0101;  // Binding Response
	public static final short MSG_BINDING_ERROR_RESPONSE =  0x0111;  // Binding Error Response
	public static final short MSG_SHARED_SECRET_REQUEST =  0x0002;  // Shared Secret Request
	public static final short MSG_SHARED_SECRET_RESPONSE =  0x0102;  // Shared Secret Response
	public static final short MSG_SHARED_SECRET_ERROR_RESPONSE =  0x0112;  // Shared Secret Error Response
	
	public static final int MAGIC_COOKIE = 0x2112A442;
	static final short HEADER_LENGTH = 20;
	
	private short msgType;
	private List<STUNAttribute> attrs;

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((attrs == null) ? 0 : attrs.hashCode());
		result = prime * result + msgType;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		STUNMessage other = (STUNMessage) obj;
		if (attrs == null) {
			if (other.attrs != null)
				return false;
		} else if (!attrs.equals(other.attrs))
			return false;
		if (msgType != other.msgType)
			return false;
		return true;
	}

	public STUNMessage(short msgType, List<STUNAttribute> attrs) {
		this.msgType = msgType;
		this.attrs = attrs;
	}
	
	/**
	 * Returns a new STUN packet from the byte array data that conforms to RFC 5389
	 * @param data
	 * @param offset
	 * @param length
	 * @param expectedTransactionID
	 * @return
	 */
	public static STUNMessage fromBytes(byte[] data, int offset, int length, STUNTransactionID expectedTransactionID) {
		if (data.length < offset + length) throw new AssertionError("Array length is smaller than offset+length.");
		if (length < HEADER_LENGTH) throw new AssertionError("Packet header length was less than the minimum header length of 20: " + data.length);
		short msgType = ByteArrayToolkit.extractShort(data, offset);
		short msgLen = ByteArrayToolkit.extractShort(data, offset+2);
		int magicCookie = ByteArrayToolkit.extractInteger(data, offset+4);
		if (MAGIC_COOKIE != magicCookie) throw new AssertionError("Magic cookie was, " + magicCookie + ", not " + MAGIC_COOKIE + ", as expected");
		if (!expectedTransactionID.equalsBytes(data, offset+8)) throw new AssertionError("The transaction ID (" + 
				ByteArrayToolkit.printArray(data, offset+8, STUNTransactionID.LENGTH, 16) + ") was not as expected (" + 
				expectedTransactionID + ")");
		
		if (msgLen + HEADER_LENGTH != length) throw new AssertionError("Length field in header (" + msgLen
				+ ") was not equal to the length given (" +  length + ") minus the header size (" + HEADER_LENGTH + ")");
		
		List<STUNAttribute> attrs = new LinkedList<STUNAttribute>();
		int curElemOffset = 0;
		while(curElemOffset < msgLen) {
			int innerElemOffset = offset + HEADER_LENGTH + curElemOffset;
			short attrType = ByteArrayToolkit.extractShort(data, innerElemOffset);
			short attrLen = ByteArrayToolkit.extractShort(data, innerElemOffset + 2);
			byte[] attrVal = new byte[attrLen];
			System.arraycopy(data, innerElemOffset + 4, attrVal, 0, attrLen);
			attrs.add(new STUNAttribute(attrType, attrVal));
			curElemOffset += 4 + getPaddedLength(attrLen);
		}
		return new STUNMessage(msgType, attrs);
	}

	/**
	 * Writes this STUN message in the packet format according to RFC 5389
	 * Returns the length of bytes that were written in the buffer.
	 * @param buffer
	 * @param offset
	 * @param transactionID
	 * @return
	 */
	public int writeBytes(byte[] buffer, int offset, STUNTransactionID transactionID) {
		//Write the message type
		ByteArrayToolkit.insertShort(msgType, buffer, offset);
		//Write the magic cookie
		ByteArrayToolkit.insertInteger(MAGIC_COOKIE, buffer, offset+4);
		//Write the transaction ID
		System.arraycopy(transactionID.getBytes(), 0, buffer, offset+8, STUNTransactionID.LENGTH);
		
		short length = 0;
		for (STUNAttribute attr : attrs) {
			int innerOffset = offset + HEADER_LENGTH + length;
			ByteArrayToolkit.insertShort(attr.getAttrType(), buffer, innerOffset);
			byte[] attrPayload = attr.getPayload();
			ByteArrayToolkit.insertShort((short)attrPayload.length, buffer, innerOffset + 2);
			System.arraycopy(attr.getPayload(), 0, buffer, innerOffset + 4, attrPayload.length);
			length += 4 + getPaddedLength(attrPayload.length);
		}
		
		// Finally, write the total length into the buffer.
		ByteArrayToolkit.insertShort(length, buffer, offset+2);
		return HEADER_LENGTH + length;
	}
	
	public List<STUNAttribute> getAttributes() {
		return attrs;
	}
	
	public STUNAttribute getFirstAttributeOfType(short type) {
		for (STUNAttribute attr : attrs) if (attr.getAttrType() == type) return attr;
		return null;
	}
	
	static int getPaddedLength(int length) {
		int mod = (length +3) % -4;
		return (length + 3) - mod;
	}
	
	public String toString() {
		return "STUNMessage(type=" + getFName(msgType) + " attrs=" + attrs + ")";
	}
	
	static Map<Integer, String> fieldNames;
	
	static {
		try {
			initConstantMap("MSG_");
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
	}
	
	public static String getFName(int constVal) {
		return fieldNames.get(constVal);
	}
	
	public static void initConstantMap(String cPrefix) throws IllegalAccessException {
		fieldNames = new HashMap<Integer, String>();
		for (Field f : STUNMessage.class.getFields()) {
			if (f.getName().startsWith(cPrefix)) fieldNames.put(f.getInt(null), f.getName());
		}
	}
	
	// ===================== ATTRIBUTE-SPECIFIC ====================
	
	public InetSocketAddress getMappedAddress() throws AddressDiscoveryException {
		STUNAttribute attr = getFirstAttributeOfType(STUNAttribute.ATTR_MAPPED_ADDRESS);
		if (attr == null) throw new AddressDiscoveryException("The given STUN message does not contain a MAPPED-ADDRESS attribute.");
		byte[] data = attr.getPayload();
		if (data.length < 8) throw new ArrayIndexOutOfBoundsException("Field is too small to contain an IP transport address (" + data.length +")");
		short type = ByteArrayToolkit.extractShort(data, 0);
		short port = ByteArrayToolkit.extractShort(data, 2);
		try {
			if (type == 1) {
				byte[] inet4addr = new byte[4];
				System.arraycopy(data, 4, inet4addr, 0, 4);
				return new InetSocketAddress(InetAddress.getByAddress(inet4addr), port & 0xffff);
			}
			if (type == 2) {
				if (data.length < 20) throw new ArrayIndexOutOfBoundsException("Field is too small to contain an IPv6 transport address (" + data.length +")");
				byte[] inet6addr = new byte[16];
				System.arraycopy(data, 4, inet6addr, 0, 16);
				return new InetSocketAddress(InetAddress.getByAddress(inet6addr), port);
			}
			
			throw new AddressDiscoveryException("The given address was neither of type IPv4 (1) nor of type IPv6 (2): Type ID was: " + type);
		} catch (UnknownHostException e) {
			throw new AddressDiscoveryException(e);
		}
	}

}
