package de.roo.util.stream;

import java.io.IOException;
import java.io.InputStream;

/**
 * Encapsulates a stream, which can only be read up to bytesToEOF
 * through this class.
 * Further reading has to be done outside the encapsulation.
 * 
 * Example:
 * 
 * InputStream blargh; (stream to encapsulate)
 * LimitedEOFStream tils = new LimitedEOFStream(blargh, 5);
 * 
 * Now "Blablub blubbel"[EOF] is coming through the stream blargh.
 * On tils, only "Blabl"[EOF] can be read.
 * Further reading on blargh results in: "ub blubbel"[EOF].
 * 
 * 
 * @author Leo Nobach
 *
 */
public class LimitedEOFStream extends ObservableInputStream {

	long c = 0;
	private final InputStream source;
	private final long bytesToEOF;
	
	public LimitedEOFStream(InputStream source, long bytesToEOF) {
		this.source = source;
		this.bytesToEOF = bytesToEOF;
	}
	
	public int read() throws IOException {
		if (c < bytesToEOF) {
			c++;
			return source.read();
		}
		return -1;
	}
	
	public int read(byte b[]) throws IOException {
		return read(b, 0, b.length);
	}
	
	public int read(byte b[], int off, int len) throws IOException {
		
		long bytesToEnd = bytesToEOF - c;
		if (bytesToEnd == 0) return -1;
		int bytesToRead = Math.min(len, (int)bytesToEnd);
		int bytesRead =  source.read(b, off, bytesToRead);
		c += bytesRead;
		return bytesRead;
	}
	
	public long skip(long n) throws IOException {
		long bytesToSkip = Math.min(n, bytesToEOF - c);
		long bytesSkipped = source.skip(bytesToSkip);
		c += bytesSkipped;
		return bytesSkipped;
	}
	
	public long skipToEOF() throws IOException {
		long allSkipped = bytesToEOF - c;
		while (c < bytesToEOF) {
			long bytesToSkip = bytesToEOF - c;
			long bytesSkipped = source.skip(bytesToSkip);
			c += bytesSkipped;
		}
		return allSkipped;
	}
	
	public int available() throws IOException {
		return Math.min((int)(bytesToEOF - c), source.available());
	}
	
	public synchronized void mark(int readlimit) {
		
	}
	
	public synchronized void reset() throws IOException {
		throw new IOException("mark/reset not supported");
	}


	public boolean markSupported() {
		return false;
	}

	@Override
	public IStreamObserver getNewStreamObserver() {
		return new StreamObserverImpl();
	}
	
	class StreamObserverImpl implements IStreamObserver {

		@Override
		public long getBytesRead() {
			return c;
		}

		@Override
		public long getTotalBytesOfStream() {
			return bytesToEOF;
		}
		
	}

}
