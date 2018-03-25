package de.roo.httpsrv;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import de.roo.logging.ILog;
import de.roo.srvApi.BadPOSTException;
import de.roo.srvApi.IPOSTFileTransferInfo;
import de.roo.srvApi.IPOSTedFile;
import de.roo.srvApi.IRequest;
import de.roo.srvApi.IRequestHandler;
import de.roo.srvApi.IServerContext;
import de.roo.srvApi.UnnecessaryPostException;
import de.roo.srvApi.IPOSTFileTransferInfo.POSTFileState;
import de.roo.util.stream.CountingOutputStream;
import de.roo.util.stream.IStreamObserver;
import de.roo.util.stream.ObservableInputStream;
import de.roo.util.stream.StreamCopy;
import de.roo.util.stream.StreamToolkit;

/**
 * 
 * @author Leo Nobach
 *
 */
public class POSTHandler {

	private static final boolean DBG_DUMP = false;
	private IRequestHandler hdlr;
	private File f;
	private IServerContext ctx;

	public POSTHandler(ILog log, IRequestHandler hdlr, IServerContext ctx) {
		this.hdlr = hdlr;
		this.ctx = ctx;
	}

	public List<IPOSTedFile> handlePOST(ObservableInputStream postStream, IRequest req)
			throws IOException, BadPOSTException {

		if (DBG_DUMP) {
			dump(postStream);
			return null;
		}

		String cTypeLn = req.getHeader("Content-Type");
		String boundary = checkCTypeGetBoundary(cTypeLn);
		//System.out.println("Boundary: " + boundary);

		String cTransEnc = req.getHeader("Content-Transfer-Encoding");
		if (cTransEnc != null)
			throw new BadPOSTException(
					"Special Transfer encodings " + cTransEnc + " currently not supported.");

		String befHdrLn;
		while(true) {
			befHdrLn = readLine(postStream);
			//System.out.println("BefHdrLn: " + befHdrLn);
			if (befHdrLn == null) return null;
			if (("--" + boundary).equals(befHdrLn)) break;
		}
			
		List<POSTedFileImpl> fList = new LinkedList<POSTedFileImpl>();
		
		while(true) {
		
			Map<String, String> headers = extractHeaders(postStream);
			
			POSTFileTransferInfoImpl info = new POSTFileTransferInfoImpl(postStream.getNewStreamObserver());
			f = hdlr.getFileNameForPOSTContent(req, headers, info, ctx);
			if (f == null) {
				throw new UnnecessaryPostException("POST will be terminated.");
			}
			boolean endOfBoundary = true;
			try {
				FileOutputStream os = new FileOutputStream(f);
				BufferedOutputStream bufO = new BufferedOutputStream(os);
				CountingOutputStream cos = new CountingOutputStream(bufO);
				endOfBoundary = writeUntilBoundaryEnd(postStream, boundary, cos, info);
				bufO.flush();
				bufO.close();
				os.close();
				if (info.getState() == POSTFileState.Running) info.finish(cos.getByteCountWritten());
			} catch (IOException e) {
				info.errorOccurred(e);
				throw e;
			} catch (RuntimeException e) {
				info.errorOccurred(e);
				throw e;
			} catch (BadPOSTException e) {
				info.errorOccurred(e);
				throw e;
			}
			
			if (endOfBoundary) break;
		}
		
		List<? extends IPOSTedFile> resBef = fList;
		return Collections.unmodifiableList(resBef);
		
		
	}

	private boolean writeUntilBoundaryEnd(InputStream source, String boundary,
			OutputStream target, IPOSTFileTransferInfo info) throws IOException, BadPOSTException {
		int b;
		byte[] boundaryBytes = ("\r\n--" + boundary).getBytes();
		byte[] afterBytes = new byte[4];
		int bytesMatching = 0;
		while ((b = source.read()) >= 0 && info.getState() == POSTFileState.Running) {
			//System.out.println("Byte:" + (char)b + " bytesMatching=" + bytesMatching + " afterBytes=" + new String(afterBytes));
			if (bytesMatching >= boundaryBytes.length) {
				int over = bytesMatching - boundaryBytes.length;
				afterBytes[over] = (byte)b;
				bytesMatching++;
				if (over == 1) {
					if (afterBytes[0] == '\r' && afterBytes[1] == '\n') {
						return false;
					}
					else if (!(afterBytes[0] == '-' && afterBytes[1] == '-')) {
						target.write(boundaryBytes);
						target.write(afterBytes[0]);
						target.write(afterBytes[1]);
						bytesMatching = 0;
					}
				} else if (over >= 3) {
					if (afterBytes[2] == '\r' && afterBytes[3] == '\n') {
						return true;
					} else {
						target.write(boundaryBytes);
						target.write(afterBytes[0]);
						target.write(afterBytes[1]);
						target.write(afterBytes[2]);
						target.write(afterBytes[3]);
						bytesMatching = 0;
					}
				}
				
				
			} else if ((byte) b == boundaryBytes[bytesMatching]) {
				bytesMatching++;
			} else {
				if (bytesMatching > 0) {
					target.write(boundaryBytes, 0, bytesMatching);
					bytesMatching = 0;
				}
				target.write(b);
			}
		}
		throw new BadPOSTException("Stream is finished but no boundary end reached. Client " + 
				"or server seem to have reset the transmission. File may be corrupted.");
	}

	private Map<String, String> extractHeaders(InputStream postStream)
			throws IOException, BadPOSTException {
		Map<String, String> headers = new HashMap<String, String>();
		String headerLine;
		while (!"".equals(headerLine = readLine(postStream))) {
			if (headerLine == null)
				return null;
			int delimiter = headerLine.indexOf(":");
			if (delimiter == -1)
				throw new BadPOSTException("Header malformed: " + headerLine);
			String key = headerLine.substring(0, delimiter);
			String value = headerLine.substring(delimiter + 1);
			if (value.startsWith(" "))
				value = value.substring(1);
			headers.put(key, value);
		}
		return headers;
	}

	private String checkCTypeGetBoundary(String cTypeLn)
			throws BadPOSTException {
		if (cTypeLn == null)
			throw new BadPOSTException("Required Content-type is missing.");
		String[] dispParts = cTypeLn.split(";");
		if (dispParts.length < 2)
			throw new BadPOSTException("Content-type " + cTypeLn
					+ "does not have a boundary string element");
		if (!dispParts[0].trim().equalsIgnoreCase("multipart/form-data"))
			throw new BadPOSTException(
					"Content-type"
							+ dispParts[0]
							+ " is not multipart/form-data, in Kangee this is required.");

		String[] boundaryParts = dispParts[1].split("=");
		if (boundaryParts.length < 2
				|| !boundaryParts[0].trim().equalsIgnoreCase("boundary"))
			throw new BadPOSTException("Boundary string" + dispParts[1]
					+ " is malformed.");
		return boundaryParts[1].trim();
	}

	private String readLine(InputStream is) throws IOException {
		return StreamToolkit.readLine(is);
	}

	private void dump(InputStream postStream) throws IOException {
		FileOutputStream fos = new FileOutputStream("postdbg.multipart");
		new StreamCopy().copy(postStream, fos);
		fos.close();
	}

	class POSTedFileImpl implements IPOSTedFile {

		private Map<String, String> headers;
		private File f;

		public POSTedFileImpl(File f, Map<String, String> headers) {
			this.f = f;
			this.headers = headers;
		}

		@Override
		public File getFile() {
			return f;
		}

		@Override
		public String getHeader(String key) {
			return headers.get(key);
		}

		@Override
		public Map<String, String> getHeadersAsMap() {
			return headers;
		}

	}
	
	static class POSTFileTransferInfoImpl implements IPOSTFileTransferInfo {
		
		private IStreamObserver obs;

		POSTFileState state = POSTFileState.Running;
		
		public POSTFileTransferInfoImpl(IStreamObserver obs) {
			this.obs = obs;
		}
		
		private long finalSize = -1;
		private List<IPOSTFileTransferInfoListener> listeners = new LinkedList<IPOSTFileTransferInfoListener>();
		private Throwable errorCause;

		@Override
		public long getTotalBytesEstim() {
			return obs.getTotalBytesOfStream();
		}

		@Override
		public long getBytesRead() {
			return obs.getBytesRead();
		}

		@Override
		public long getFinalSize() {
			return finalSize;
		}
		
		public void finish(long finalSize) {
			this.finalSize = finalSize;
			if (finalSize <= 0)
				state = POSTFileState.No_File;
			else
				state = POSTFileState.Success;
			this.stateChanged();
		}

		@Override
		public void addListener(IPOSTFileTransferInfoListener l) {
			listeners.add(l);
		}

		@Override
		public void removeListener(IPOSTFileTransferInfoListener l) {
			listeners.remove(l);
		}
		
		public void errorOccurred(Throwable e) {
			this.errorCause = e;
			state = POSTFileState.Error;
			this.stateChanged();
		}
		
		void stateChanged() {
			for (IPOSTFileTransferInfoListener l : listeners) l.stateChanged();
		}

		@Override
		public Throwable getErrorCause() {
			return this.errorCause;
		}

		@Override
		public POSTFileState getState() {
			return state;
		}

		@Override
		public void terminate() {
			state = POSTFileState.Cancel;
			this.stateChanged();
		}
		
	}

}
