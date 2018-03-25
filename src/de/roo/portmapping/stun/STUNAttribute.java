package de.roo.portmapping.stun;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * 
 * @author Leo Nobach
 *
 */
public class STUNAttribute {

	short attrType;
	byte[] payload;
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + attrType;
		result = prime * result + Arrays.hashCode(payload);
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
		STUNAttribute other = (STUNAttribute) obj;
		if (attrType != other.attrType)
			return false;
		if (!Arrays.equals(payload, other.payload))
			return false;
		return true;
	}

	public STUNAttribute(short attrType, byte[] payload) {
		this.attrType = attrType;
		this.payload = payload;
	}
	
	public short getAttrType() {
		return attrType;
	}
	
	public byte[] getPayload() {
		return payload;
	}
	
	public int getPayloadLength() {
		return payload.length;
	}
	
	public static final short ATTR_MAPPED_ADDRESS = 0x0001; // MAPPED-ADDRESS
	public static final short ATTR_RESPONSE_ADDRESS = 0x0002; // RESPONSE-ADDRESS
	public static final short ATTR_CHANGE_REQUEST = 0x0003; // CHANGE-REQUEST
	public static final short ATTR_SOURCE_ADDRESS = 0x0004; // SOURCE-ADDRESS
	public static final short ATTR_CHANGED_ADDRESS = 0x0005; // CHANGED-ADDRESS
	public static final short ATTR_USERNAME = 0x0006; // USERNAME
	public static final short ATTR_PASSWORD = 0x0007; // PASSWORD
	public static final short ATTR_MESSAGE_INTEGRITY = 0x0008; // MESSAGE-INTEGRITY
	public static final short ATTR_ERROR_CODE = 0x0009; // ERROR-CODE
	public static final short ATTR_UNKNOWN_ATTRIBUTES = 0x000a; // UNKNOWN-ATTRIBUTES
	public static final short ATTR_REFLECTED_FORM = 0x000b; // REFLECTED-FROM
	public static final short ATTR_REALM = 0x0014; // REALM
	public static final short ATTR_NONCE = 0x0015; // NONCE
	public static final short ATTR_XOR_MAPPED_ADDRESS =  0x0020; // XOR-MAPPED-ADDRESS
	public static final short ATTR_SOFTWARE = (short)0x8022; // SOFTWARE
	public static final short ATTR_ALTERNATE_SERVER = (short)0x8023; //ALTERNATE-SERVER
	public static final short ATTR_FINGERPRINT = (short)0x8028; // FINGERPRINT

	
	static Map<Integer, String> fieldNames;
	
	static {
		try {
			initConstantMap("ATTR_");
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
	}
	
	public static String getFName(int constVal) {
		String result = fieldNames.get(constVal);
		if (result == null) result = Integer.toHexString(constVal & 0xFFFF);
		return result;
	}
	
	public static void initConstantMap(String cPrefix) throws IllegalAccessException {
		fieldNames = new HashMap<Integer, String>();
		for (Field f : STUNAttribute.class.getFields()) {
			if (f.getName().startsWith(cPrefix)) fieldNames.put(f.getInt(null), f.getName());
		}
	}
	
	public String toString() {
		return "(type=" + getFName(attrType) + ", length=" + payload.length + ")";
	}
	
}
