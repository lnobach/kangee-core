package de.roo.httpcli;

import java.io.IOException;
import java.io.InputStream;

import de.roo.util.stream.StreamToolkit;

/**
 * 
 * @author Leo Nobach
 *
 */
public class ChunkHandlingStream extends InputStream {

	static final String ret = "\r\n";
	
	private InputStream src;
	long nextChunk = 0;
	boolean start = true;

	public ChunkHandlingStream(InputStream chunkedStream) {
		this.src = chunkedStream;
	}
	
	@Override
	public int read() throws IOException {
		if (nextChunk == 0) readChunkInfo();
		if (nextChunk == -1) return -1;
		nextChunk--;
		return src.read();
	}

	public int read(byte b[]) throws IOException {
        return read(b, 0, b.length);
    }
    
    public int read(byte b[], int off, int len) throws IOException {
    	if (nextChunk == 0) readChunkInfo();
    	if (nextChunk == -1) return -1;
    	int diff = len-off;
    	int pot = (int)Math.min(diff, nextChunk);
    	int result = src.read(b, off, pot);
    	if (result != -1) nextChunk -= result;
    	return result;
    }
	
    public long skip(long n) throws IOException {
    	if (nextChunk == 0) readChunkInfo();
    	if (nextChunk == -1) return 0;
    	long pot = Math.min(n, nextChunk);
    	long result = src.skip(pot);
    	nextChunk -= result;
    	return result;
    }
    	
    public int available() throws IOException {
    	if (nextChunk == -1) return 0;
    	return (int)Math.min((long)Integer.MAX_VALUE, nextChunk);
    }
    
    private void readChunkInfo() throws IOException {
		if (!start) {
			String l = StreamToolkit.readLine(src);
			if (!"".equals(l)) throw new AssertionError("The line that should be empty was: '" + l + "'");
			
		} else start = false;
		String hexChunkLen = StreamToolkit.readLine(src);
		if (hexChunkLen == null) nextChunk = -1;
		try {
			long chunkLength = Long.decode("0x" + hexChunkLen);
			if (chunkLength < 0) throw new IOException("Chunk length is negative: " + chunkLength);
			if (chunkLength == 0) {
				chunkLength = -1;
				String l = StreamToolkit.readLine(src);
				if (!"".equals(l)) throw new AssertionError("The final line that should be empty was: '" + l + "'");
			}
			nextChunk = chunkLength;
		} catch (NumberFormatException e) {
			throw new IOException("Could not read chunk length since it is malformed: " + hexChunkLen, e);
		}
	}

	public void skipToEOF() throws IOException {
		if (nextChunk == -1) return;
		do {
			src.skip(nextChunk);
			readChunkInfo();
		} while (nextChunk >= 0);
	}
}








