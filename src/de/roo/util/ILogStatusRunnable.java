package de.roo.util;

import de.roo.logging.ILog;

/**
 * 
 * @author Leo Nobach
 *
 */
public interface ILogStatusRunnable {

	/**
	 * The process to run. Should periodically check hdlr.wasTerminated() and if
	 * so, should close.
	 * Returns true iff run was successful.
	 * @param hdlr
	 * @param log
	 */
	public boolean run(ILogStatusHandler hdlr, ILog log) throws InterruptedException;
	
	public interface ILogStatusHandler {
		
		public void setCurrentJob(String status);
		
		/**
		 *
		 * @param progress 0 - 1
		 */
		public void setCurrentProgress(double progress);
		
		public boolean wasTerminated();
		
	}
	
}
