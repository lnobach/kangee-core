package de.roo.portmapping.stun;

import java.util.Arrays;
import java.util.Random;

import de.roo.util.ByteArrayToolkit;

/**
 * 
 * @author Leo Nobach
 *
 */
public class STUNTransactionID {

	public static final int LENGTH = 12;
	
	byte[] bytes;
	
	private STUNTransactionID() {
		
	}
	
	public static STUNTransactionID newRandomTransactionID() {
		return newRandomTransactionID(new Random());
	}
	
	public static STUNTransactionID newRandomTransactionID(Random random) {
		STUNTransactionID result = new STUNTransactionID();
		result.bytes = new byte[LENGTH];
		random.nextBytes(result.bytes);
		return result;
	}
	
	public boolean equalsBytes(byte[] bytes, int offset) {
		return ByteArrayToolkit.equals(this.bytes, 0, bytes, offset, LENGTH);
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(bytes);
		return result;
	}
	
	public String toString() {
		return ByteArrayToolkit.printArray(bytes, 0, LENGTH, 16);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		STUNTransactionID other = (STUNTransactionID) obj;
		if (!Arrays.equals(bytes, other.bytes))
			return false;
		return true;
	}

	public byte[] getBytes() {
		return bytes;
	}
	
}
