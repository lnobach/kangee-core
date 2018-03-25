package de.roo.logging;

import java.util.LinkedList;
import java.util.List;

/**
 * 
 * @author Leo Nobach
 *
 */
public class LogTree implements ILog {

	List<ILog> sublogs = new LinkedList<ILog>();
	
	Object treeLock = new Object();
	
	public void addLog(ILog log) {
		sublogs.add(log);
	}
	
	public void removeLog(ILog log) {
		sublogs.remove(log);
	}

	@Override
	public void dbg(Object src, Object msg) {
		for (ILog log : sublogs) log.dbg(src, msg);
	}

	@Override
	public void error(Object src, Object msg) {
		for (ILog log : sublogs) log.error(src, msg);
	}

	@Override
	public void error(Object src, Object msg, Throwable ex) {
		for (ILog log : sublogs) log.error(src, msg, ex);
	}

	@Override
	public void warn(Object src, Object msg) {
		for (ILog log : sublogs) log.warn(src, msg);
	}

	@Override
	public void warn(Object src, Object msg, Throwable ex) {
		for (ILog log : sublogs) log.warn(src, msg, ex);
	}
	
}
