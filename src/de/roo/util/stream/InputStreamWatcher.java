package de.roo.util.stream;

import java.io.IOException;
import java.io.InputStream;

/**
 * Encapsulates a stream, which can only be read up to bytesToEOF
 * through this class.
 * Further reading has to be done outside the encapsulation.
 * @author Leo Nobach
 *
 */
public class InputStreamWatcher extends InputStream {

	long c = 0;
	private final InputStream source;
	
	public InputStreamWatcher(InputStream source) {
		this.source = source;
	}
	
	public int read() throws IOException {
		System.out.println("WATCH: read()");
		return source.read();
	}
	
	public int read(byte b[]) throws IOException {
		System.out.println("WATCH: read(byte b[]))");
		return source.read(b, 0, b.length);
	}
	
	public int read(byte b[], int off, int len) throws IOException {
		System.out.println("WATCH: read(byte b[], int off, int len)");
		return source.read(b, off, len);
	}
	
	public long skip(long n) throws IOException {
		System.out.println("WATCH: skip(long n)");
		return source.skip(n);
	}
	
	public int available() throws IOException {
		System.out.println("WATCH: available()");
		return source.available();
	}
	
	public synchronized void mark(int readlimit) {
		System.out.println("WATCH: mark(int readlimit)");
		source.mark(readlimit);
	}
	
	public synchronized void reset() throws IOException {
		System.out.println("WATCH: reset");
		source.reset();
	}

	public boolean markSupported() {
		System.out.println("WATCH: markSupported()");
		return source.markSupported();
	}

}

