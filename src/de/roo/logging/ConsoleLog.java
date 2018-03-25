package de.roo.logging;

/**
 * 
 * @author Leo Nobach
 *
 */
public class ConsoleLog implements ILog {

	@Override
	public void dbg(Object src, Object msg) {
		System.out.println("DBG:"+ getSrcName(src) + ": " + msg);
	}

	@Override
	public void error(Object src, Object msg) {
		System.out.println("ERR:" + getSrcName(src) + ": " + msg);
	}

	@Override
	public void error(Object src, Object msg, Throwable ex) {
		System.err.println("ERR:"+ getSrcName(src) + ": " + msg);
		ex.printStackTrace();
	}

	@Override
	public void warn(Object src, Object msg) {
		System.out.println("WRN:"+ getSrcName(src) + ": " + msg);
	}

	public String getSrcName(Object src) {
		if (src instanceof Class) return ((Class<?>)src).getSimpleName();
		return src.getClass().getSimpleName();
	}

	@Override
	public void warn(Object src, Object msg, Throwable ex) {
		System.out.println("WRN:"+ getSrcName(src) + ": " + msg);
		ex.printStackTrace();
	}
	
}
