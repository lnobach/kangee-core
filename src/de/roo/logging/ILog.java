package de.roo.logging;

/**
 * 
 * @author Leo Nobach
 *
 */
public interface ILog {

	public void error(Object src, Object msg);
	
	public void error(Object src, Object msg, Throwable ex);
	
	public void warn(Object src, Object msg);
	
	public void warn(Object src, Object msg, Throwable ex);
	
	public void dbg(Object src, Object msg);
	
}
