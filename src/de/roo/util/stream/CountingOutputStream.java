package de.roo.util.stream;

import java.io.IOException;
import java.io.OutputStream;

/**
 * 
 * @author Leo Nobach
 *
 */
public class CountingOutputStream extends OutputStream {

	long c = 0;
	private OutputStream os;
	
	public CountingOutputStream(OutputStream os) {
		this.os = os;
	}
	
	@Override
	public void write(int b) throws IOException {
		os.write(b);
		c++;
	}
	
    public void write(byte b[]) throws IOException {
    	os.write(b);
    	c += b.length;
    }

    public void write(byte b[], int off, int len) throws IOException {
    	os.write(b, off, len);
    	c += len;
    }
    
    public void flush() throws IOException {
    	os.flush();
    }

    public void close() throws IOException {
    	os.close();
    }
    
    public long getByteCountWritten() {
    	return c;
    }
	
}
