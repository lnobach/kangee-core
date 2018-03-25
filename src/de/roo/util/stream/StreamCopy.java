package de.roo.util.stream;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * 
 * @author Leo Nobach
 *
 */
public class StreamCopy {

	static final int DEFAULT_BUFFER_SIZE = 2048;
	
	long bytesCopied = 0;

	private int bufferSize;

	private boolean terminated;
	
	public StreamCopy() {
		this(DEFAULT_BUFFER_SIZE);
	}
	
	public StreamCopy(int bufferSize) {
		this.bufferSize = bufferSize;
	}

	/**
	 * Copies is to os until EOF of is
	 * @param is
	 * @param os
	 * @return if the stream was not terminated asynchronously
	 * @throws IOException
	 */
	public boolean copy(InputStream is, OutputStream os) throws IOException {
		bytesCopied = 0;
		
		byte[] buffer = new byte[bufferSize];
		
		int len; 
		while ((len = is.read(buffer)) > 0) {
			if (terminated) return false;
			os.write(buffer, 0, len);
			bytesCopied += len;
		}
		
		return true;
	}
	
	/**
	 * Here, the stream copy can be stopped asynchronously
	 */
	public void terminate() {
		terminated = true;
	}
	
	public long getBytesCopied() {
		return bytesCopied;
	}
	
	public static byte[] getByteArrayFromInputStream(InputStream is) throws IOException {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		new StreamCopy().copy(is, os);
		return os.toByteArray();
	}
	
}
