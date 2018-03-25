package de.roo.srvApi;

/**
 * 
 * @author Leo Nobach
 *
 */
public interface IPOSTFileTransferInfo {
	
	public enum POSTFileState {
		Running,
		Success,
		Error,
		Cancel, 
		No_File;
	}
	
	public long getBytesRead();
	
	public long getTotalBytesEstim();
	
	public POSTFileState getState();
	
	public long getFinalSize();
	
	public void addListener(IPOSTFileTransferInfoListener l);
	
	public void removeListener(IPOSTFileTransferInfoListener l);
	
	public void terminate();
	
	public Throwable getErrorCause();
	
	public interface IPOSTFileTransferInfoListener {
		
		public void stateChanged();
		
	}
	
}
